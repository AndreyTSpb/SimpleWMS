package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.domain.PickingRouteStatus;
import com.tynyany.simplewmsv2.domain.PlacementRouteStatus;
import com.tynyany.simplewmsv2.domain.ReceivingStatus;
import com.tynyany.simplewmsv2.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Отчёты строятся SQL-запросами только на чтение (NamedParameterJdbcTemplate):
 * так проще собирать фильтры и агрегаты, чем через репозитории сущностей.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultReportService implements ReportService {

    static final int PAGE_SIZE = 50;

    private final NamedParameterJdbcTemplate jdbc;
    private final StockRepository stockRepository;
    private final WmsProperties wmsProperties;

    // ---------- состояние склада ----------

    @Override
    public List<BatchStockRow> stockByBatch(StockFilter filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder(
                "SELECT p.product_code, p.product_name, s.batch_id, b.expiry_date, s.location_id, l.location_code," +
                " z.code AS zone_code, s.quantity," +
                " COALESCE((SELECT SUM(r.quantity) FROM stock_reservation r" +
                "   WHERE r.batch_id = s.batch_id AND r.location_id = s.location_id), 0) AS reserved" +
                " FROM stocks s" +
                " JOIN product p ON p.productid = s.product_id" +
                " JOIN batches b ON b.batch_id = s.batch_id" +
                " JOIN location l ON l.locationid = s.location_id" +
                " LEFT JOIN zones z ON z.zone_id = l.zoneid" +
                " WHERE s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE");
        appendProductFilter(sql, params, filter.product());
        appendLocationFilter(sql, params, filter.zoneId(), filter.location());
        if (filter.expiringOnly()) {
            sql.append(" AND b.expiry_date IS NOT NULL AND b.expiry_date < :warnDate");
            params.addValue("warnDate", Timestamp.valueOf(warningDate().atStartOfDay()));
        }
        sql.append(" ORDER BY p.product_name, b.expiry_date ASC NULLS LAST, l.location_code");

        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
        LocalDate today = LocalDate.now();
        int warningDays = wmsProperties.getExpiry().getWarningDays();
        return jdbc.query(sql.toString(), params, (rs, i) -> {
            Timestamp expiry = rs.getTimestamp("expiry_date");
            return new BatchStockRow(rs.getString("product_code"), rs.getString("product_name"), rs.getInt("batch_id"),
                    expiry != null ? date.format(expiry) : "",
                    expiryState(expiry != null ? expiry.toLocalDateTime().toLocalDate() : null, today, warningDays),
                    rs.getInt("location_id"), rs.getString("location_code"), nvl(rs.getString("zone_code")),
                    rs.getInt("quantity"), rs.getInt("reserved"));
        });
    }

    @Override
    public List<ProductStockRow> stockByProduct(StockFilter filter) {
        return aggregateByProduct(stockByBatch(filter), wmsProperties.getLocations());
    }

    /** Свод остатков по товарам из строк по партиям и ячейкам. */
    static List<ProductStockRow> aggregateByProduct(List<BatchStockRow> rows, WmsProperties.Locations system) {
        Map<String, int[]> totals = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, ExpiryState> worst = new LinkedHashMap<>();
        for (BatchStockRow row : rows) {
            int[] t = totals.computeIfAbsent(row.productCode(), k -> new int[5]);
            names.putIfAbsent(row.productCode(), row.productName());
            t[0] += row.quantity();
            if (row.locationId() == system.getReceivingId()) {
                t[3] += row.quantity();
            } else if (row.locationId() == system.getShippingId()) {
                t[4] += row.quantity();
            } else {
                t[1] += row.quantity();
                t[2] += row.reserved();
            }
            worst.merge(row.productCode(), row.expiryState(), (a, b) -> a.ordinal() >= b.ordinal() ? a : b);
        }
        List<ProductStockRow> result = new ArrayList<>();
        totals.forEach((code, t) -> result.add(new ProductStockRow(code, names.get(code),
                t[0], t[1], t[2], t[3], t[4], worst.get(code))));
        return result;
    }

    @Override
    public List<CellLoadRow> cellLoad(StockFilter filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("system", List.of(wmsProperties.getLocations().getReceivingId(),
                wmsProperties.getLocations().getShippingId()));
        StringBuilder sql = new StringBuilder(
                "SELECT l.location_code, z.code AS zone_code, l.capacity," +
                " COALESCE(SUM(s.quantity * COALESCE(p.volume, 0)), 0) AS used, COUNT(s.stock_id) AS positions" +
                " FROM location l" +
                " LEFT JOIN zones z ON z.zone_id = l.zoneid" +
                " LEFT JOIN stocks s ON s.location_id = l.locationid AND s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE" +
                " LEFT JOIN product p ON p.productid = s.product_id" +
                " WHERE l.del = 0 AND l.locationid NOT IN (:system)");
        appendLocationFilter(sql, params, filter.zoneId(), filter.location());
        sql.append(" GROUP BY l.locationid, l.location_code, z.code, l.capacity ORDER BY l.location_code");
        return jdbc.query(sql.toString(), params, (rs, i) -> new CellLoadRow(rs.getString("location_code"),
                nvl(rs.getString("zone_code")), round(rs.getDouble("capacity")), round(rs.getDouble("used")),
                rs.getInt("positions")));
    }

    // ---------- журнал движений ----------

    @Override
    public MovementPage movements(MovementFilter filter, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE COALESCE(m.deleted, FALSE) = FALSE");
        if (filter.from() != null) {
            where.append(" AND m.movement_date >= :from");
            params.addValue("from", Timestamp.valueOf(filter.from().atStartOfDay()));
        }
        if (filter.to() != null) {
            where.append(" AND m.movement_date < :to");
            params.addValue("to", Timestamp.valueOf(filter.to().plusDays(1).atStartOfDay()));
        }
        if (filter.type() != null) {
            where.append(" AND m.movement_type_id = :type");
            params.addValue("type", filter.type());
        }
        if (filter.batchId() != null) {
            where.append(" AND m.batch_id = :batch");
            params.addValue("batch", filter.batchId());
        }
        appendProductFilter(where, params, filter.product());

        String from = " FROM stock_movements m" +
                " LEFT JOIN batches b ON b.batch_id = m.batch_id" +
                " LEFT JOIN product p ON p.productid = b.product_id" +
                " LEFT JOIN location lf ON lf.locationid = m.from_location_id" +
                " LEFT JOIN location lt ON lt.locationid = m.to_location_id" +
                " LEFT JOIN employee e ON e.employeeid = m.employeeid";
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + from + where, params, Long.class);
        long count = total == null ? 0 : total;
        int pages = (int) Math.max(1, (count + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(page, 1), pages);
        params.addValue("limit", PAGE_SIZE);
        params.addValue("offset", (current - 1) * PAGE_SIZE);

        SimpleDateFormat dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        List<MovementRow> rows = jdbc.query(
                "SELECT m.movement_date, m.movement_type_id, p.product_code, p.product_name, m.batch_id," +
                " lf.location_code AS from_code, lt.location_code AS to_code, m.quantity, e.employee_name" +
                from + where + " ORDER BY m.movement_id DESC LIMIT :limit OFFSET :offset",
                params, (rs, i) -> {
                    Timestamp date = rs.getTimestamp("movement_date");
                    return new MovementRow(date != null ? dateTime.format(date) : "",
                            movementTitle(rs.getInt("movement_type_id")),
                            nvl(rs.getString("product_code")), nvl(rs.getString("product_name")), rs.getInt("batch_id"),
                            dash(rs.getString("from_code")), dash(rs.getString("to_code")),
                            rs.getInt("quantity"), nvl(rs.getString("employee_name")));
                });
        return new MovementPage(rows, current, pages, count);
    }

    // ---------- сводки за период ----------

    @Override
    public ShipmentReport shipmentReport(LocalDate from, LocalDate to) {
        MapSqlParameterSource params = period(from, to);
        String period = " WHERE COALESCE(sh.del, FALSE) = FALSE AND sh.shipped_at >= :from AND sh.shipped_at < :to";
        List<ShipmentByCustomer> customers = jdbc.query(
                "SELECT c.customer_name, c.customer_code, COUNT(DISTINCT sh.shipment_id) AS docs," +
                " COALESCE(SUM(sl.quantity), 0) AS qty, COALESCE(SUM(sl.quantity * COALESCE(p.weight, 0)), 0) AS weight" +
                " FROM shipment sh" +
                " JOIN customer_order o ON o.order_id = sh.order_id" +
                " JOIN customers c ON c.customer_id = o.customer_id" +
                " LEFT JOIN shipment_line sl ON sl.shipment_id = sh.shipment_id" +
                " LEFT JOIN product p ON p.productid = sl.product_id" + period +
                " GROUP BY c.customer_name, c.customer_code ORDER BY qty DESC, c.customer_name",
                params, (rs, i) -> new ShipmentByCustomer(
                        rs.getString("customer_name") + " (" + rs.getString("customer_code") + ")",
                        rs.getInt("docs"), rs.getInt("qty"), round(rs.getDouble("weight"))));
        List<ShipmentByProduct> products = jdbc.query(
                "SELECT p.product_code, p.product_name, SUM(ol.qty_ordered) AS ordered, SUM(ol.qty_shipped) AS shipped" +
                " FROM shipment sh" +
                " JOIN customer_order_line ol ON ol.order_id = sh.order_id" +
                " JOIN product p ON p.productid = ol.product_id" + period +
                " GROUP BY p.product_code, p.product_name ORDER BY shipped DESC, p.product_name",
                params, (rs, i) -> new ShipmentByProduct(rs.getString("product_code"), rs.getString("product_name"),
                        rs.getInt("ordered"), rs.getInt("shipped")));
        int shipments = customers.stream().mapToInt(ShipmentByCustomer::shipments).sum();
        int quantity = customers.stream().mapToInt(ShipmentByCustomer::quantity).sum();
        double weight = round(customers.stream().mapToDouble(ShipmentByCustomer::weight).sum());
        return new ShipmentReport(customers, products, shipments, quantity, weight);
    }

    @Override
    public IncomeReport incomeReport(LocalDate from, LocalDate to) {
        MapSqlParameterSource params = period(from, to);
        params.addValue("completed", ReceivingStatus.COMPLETED.getCode());
        String base = " FROM receiving rc" +
                " JOIN receiving_line rl ON rl.receivingid = rc.receivingid AND COALESCE(rl.del, FALSE) = FALSE" +
                " WHERE COALESCE(rc.del, FALSE) = FALSE AND rc.statusid = :completed" +
                "   AND COALESCE(rc.get_receiving_date, rc.receiving_date) >= :from" +
                "   AND COALESCE(rc.get_receiving_date, rc.receiving_date) < :to";
        List<IncomeBySupplier> suppliers = jdbc.query(
                "SELECT COALESCE(s.supplier_name, 'поставщик ' || rc.supplierid) AS supplier," +
                " COUNT(DISTINCT rc.receivingid) AS docs, COUNT(rl.receiving_lineid) AS lines," +
                " COALESCE(SUM(rl.quantity_product), 0) AS expected, COALESCE(SUM(rl.quantity_received), 0) AS received" +
                base.replace(" WHERE", " LEFT JOIN supplier s ON s.supplierid = rc.supplierid WHERE") +
                " GROUP BY 1 ORDER BY received DESC, 1",
                params, (rs, i) -> new IncomeBySupplier(rs.getString("supplier"), rs.getInt("docs"), rs.getInt("lines"),
                        rs.getInt("expected"), rs.getInt("received")));
        List<IncomeByProduct> products = jdbc.query(
                "SELECT p.product_code, p.product_name," +
                " COALESCE(SUM(rl.quantity_product), 0) AS expected, COALESCE(SUM(rl.quantity_received), 0) AS received" +
                base.replace(" WHERE", " JOIN product p ON p.productid = rl.productid WHERE") +
                " GROUP BY p.product_code, p.product_name ORDER BY received DESC, p.product_name",
                params, (rs, i) -> new IncomeByProduct(rs.getString("product_code"), rs.getString("product_name"),
                        rs.getInt("expected"), rs.getInt("received")));
        return new IncomeReport(suppliers, products,
                suppliers.stream().mapToInt(IncomeBySupplier::documents).sum(),
                suppliers.stream().mapToInt(IncomeBySupplier::expected).sum(),
                suppliers.stream().mapToInt(IncomeBySupplier::received).sum());
    }

    @Override
    public List<EmployeeWorkRow> employeeWork(LocalDate from, LocalDate to) {
        MapSqlParameterSource params = period(from, to);
        params.addValue("pickingDone", PickingRouteStatus.COMPLETED.getCode());
        params.addValue("placementDone", PlacementRouteStatus.COMPLETED.getCode());
        Map<String, int[]> work = new LinkedHashMap<>();
        jdbc.query(
                "SELECT COALESCE(e.employee_name, 'не указан') AS employee, COUNT(DISTINCT r.route_id) AS routes," +
                " COUNT(l.line_id) AS lines, COALESCE(SUM(l.qty_picked), 0) AS qty," +
                " COUNT(l.line_id) FILTER (WHERE l.qty_picked < l.qty_planned) AS short_lines," +
                " COALESCE(SUM(l.qty_planned - l.qty_picked), 0) AS short_qty" +
                " FROM picking_route r" +
                " JOIN picking_route_line l ON l.route_id = r.route_id" +
                " LEFT JOIN employee e ON e.employeeid = r.employee_id" +
                " WHERE COALESCE(r.del, FALSE) = FALSE AND r.status_id = :pickingDone" +
                "   AND r.finished_at >= :from AND r.finished_at < :to" +
                " GROUP BY 1",
                params, rs -> {
                    int[] w = work.computeIfAbsent(rs.getString("employee"), k -> new int[8]);
                    w[0] = rs.getInt("routes");
                    w[1] = rs.getInt("lines");
                    w[2] = rs.getInt("qty");
                    w[3] = rs.getInt("short_lines");
                    w[4] = rs.getInt("short_qty");
                });
        jdbc.query(
                "SELECT COALESCE(e.employee_name, 'не указан') AS employee, COUNT(DISTINCT r.p_route_id) AS routes," +
                " COUNT(l.p_route_line_id) AS lines, COALESCE(SUM(l.qnt), 0) AS qty" +
                " FROM placement_route r" +
                " JOIN placement_route_line l ON l.p_route_id = r.p_route_id AND COALESCE(l.del, FALSE) = FALSE" +
                " LEFT JOIN employee e ON e.employeeid = r.employer_id" +
                " WHERE COALESCE(r.del, FALSE) = FALSE AND r.status = :placementDone" +
                "   AND r.dt_stop >= :from AND r.dt_stop < :to" +
                " GROUP BY 1",
                params, rs -> {
                    int[] w = work.computeIfAbsent(rs.getString("employee"), k -> new int[8]);
                    w[5] = rs.getInt("routes");
                    w[6] = rs.getInt("lines");
                    w[7] = rs.getInt("qty");
                });
        List<EmployeeWorkRow> result = new ArrayList<>();
        work.forEach((employee, w) -> result.add(new EmployeeWorkRow(employee, w[0], w[1], w[2], w[3], w[4], w[5], w[6], w[7])));
        result.sort((a, b) -> Integer.compare(b.pickedQuantity() + b.placedQuantity(), a.pickedQuantity() + a.placedQuantity()));
        return result;
    }

    // ---------- главная страница ----------

    @Override
    public Dashboard dashboard() {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("today", Timestamp.valueOf(LocalDate.now().atStartOfDay()))
                .addValue("warnDate", Timestamp.valueOf(warningDate().atStartOfDay()));
        Map<Integer, Integer> receivings = countByStatus(
                "SELECT statusid, COUNT(*) FROM receiving WHERE COALESCE(del, FALSE) = FALSE GROUP BY statusid");
        Map<Integer, Integer> orders = countByStatus(
                "SELECT status_id, COUNT(*) FROM customer_order WHERE COALESCE(del, FALSE) = FALSE GROUP BY status_id");
        Map<Integer, Integer> placement = countByStatus(
                "SELECT status, COUNT(*) FROM placement_route WHERE COALESCE(del, FALSE) = FALSE GROUP BY status");
        Map<Integer, Integer> picking = countByStatus(
                "SELECT status_id, COUNT(*) FROM picking_route WHERE COALESCE(del, FALSE) = FALSE GROUP BY status_id");
        String batches = "SELECT COUNT(DISTINCT s.batch_id) FROM stocks s JOIN batches b ON b.batch_id = s.batch_id" +
                " WHERE s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE AND b.expiry_date IS NOT NULL";
        Integer expired = jdbc.queryForObject(batches + " AND b.expiry_date < :today", params, Integer.class);
        Integer expiring = jdbc.queryForObject(batches + " AND b.expiry_date >= :today AND b.expiry_date < :warnDate",
                params, Integer.class);
        return new Dashboard(
                receivings.getOrDefault(ReceivingStatus.WAITING.getCode(), 0),
                receivings.getOrDefault(ReceivingStatus.IN_PROGRESS.getCode(), 0),
                stockRepository.findUnplaced(wmsProperties.getLocations().getReceivingId()).size(),
                placement.getOrDefault(PlacementRouteStatus.CREATED.getCode(), 0)
                        + placement.getOrDefault(PlacementRouteStatus.IN_PROGRESS.getCode(), 0),
                orders.getOrDefault(OrderStatus.NEW.getCode(), 0)
                        + orders.getOrDefault(OrderStatus.PARTIALLY_RESERVED.getCode(), 0),
                orders.getOrDefault(OrderStatus.RESERVED.getCode(), 0),
                orders.getOrDefault(OrderStatus.PICKING.getCode(), 0),
                orders.getOrDefault(OrderStatus.PICKED.getCode(), 0),
                picking.getOrDefault(PickingRouteStatus.CREATED.getCode(), 0)
                        + picking.getOrDefault(PickingRouteStatus.IN_PROGRESS.getCode(), 0),
                expiring == null ? 0 : expiring,
                expired == null ? 0 : expired);
    }

    // ---------- вспомогательные ----------

    /** Состояние срока годности: истёк, истекает в ближайшие warningDays дней, в порядке или срока нет. */
    static ExpiryState expiryState(LocalDate expiry, LocalDate today, int warningDays) {
        if (expiry == null) return ExpiryState.NONE;
        if (expiry.isBefore(today)) return ExpiryState.EXPIRED;
        if (expiry.isBefore(today.plusDays(warningDays))) return ExpiryState.SOON;
        return ExpiryState.OK;
    }

    private LocalDate warningDate() {
        return LocalDate.now().plusDays(wmsProperties.getExpiry().getWarningDays());
    }

    private static void appendProductFilter(StringBuilder sql, MapSqlParameterSource params, String product) {
        if (product == null || product.isBlank()) return;
        sql.append(" AND (p.product_code ILIKE :productLike OR p.product_name ILIKE :productLike" +
                " OR p.ext_bar_code = :product OR p.int_bar_code = :product)");
        params.addValue("productLike", "%" + escapeLike(product.trim()) + "%");
        params.addValue("product", product.trim());
    }

    private static void appendLocationFilter(StringBuilder sql, MapSqlParameterSource params, Integer zoneId, String location) {
        if (zoneId != null) {
            sql.append(" AND l.zoneid = :zoneId");
            params.addValue("zoneId", zoneId);
        }
        if (location != null && !location.isBlank()) {
            sql.append(" AND l.location_code ILIKE :location");
            params.addValue("location", "%" + escapeLike(location.trim()) + "%");
        }
    }

    private Map<Integer, Integer> countByStatus(String sql) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        jdbc.query(sql, new MapSqlParameterSource(), rs -> {
            result.put(rs.getInt(1), rs.getInt(2));
        });
        return result;
    }

    private static MapSqlParameterSource period(LocalDate from, LocalDate to) {
        return new MapSqlParameterSource()
                .addValue("from", Timestamp.valueOf(from.atStartOfDay()))
                .addValue("to", Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
    }

    private static String movementTitle(int code) {
        try {
            return MovementType.fromCode(code).getTitle();
        } catch (IllegalArgumentException e) {
            return "тип " + code;
        }
    }

    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private static String dash(String value) {
        return value == null ? "—" : value;
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
