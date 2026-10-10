package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.StockReservationEntity;
import com.tynyany.simplewmsv2.dao.ZoneEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.domain.PickingRouteStatus;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderLineRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteLineRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import com.tynyany.simplewmsv2.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPickingService implements PickingService {

    private static final List<Integer> OPEN_STATUSES = List.of(
            PickingRouteStatus.CREATED.getCode(), PickingRouteStatus.IN_PROGRESS.getCode());

    private final PickingRouteRepository routeRepository;
    private final PickingRouteLineRepository routeLineRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderLineRepository orderLineRepository;
    private final StockReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final BatchRepository batchRepository;
    private final EmployeeRepository employeeRepository;
    private final InventoryService inventoryService;
    private final WmsProperties wmsProperties;

    @Override
    @Transactional
    public int createRoute(int orderId) {
        CustomerOrderEntity order = getOrderEntity(orderId);
        OrderStatus status = OrderStatus.fromCode(order.getStatusId());
        if (status != OrderStatus.RESERVED) {
            throw new WmsException("Передать в сборку можно только полностью зарезервированный заказ, текущий статус: «"
                    + status.getTitle() + "»");
        }
        if (!routeRepository.findByOrderAndStatuses(orderId, OPEN_STATUSES).isEmpty()) {
            throw new WmsException("Заказ " + order.getDocumentNumber() + " уже передан в сборку");
        }
        List<StockReservationEntity> reservations = reservationRepository.findByOrderIdOrderByReservationIdAsc(orderId);
        if (reservations.isEmpty()) {
            throw new WmsException("В заказе " + order.getDocumentNumber() + " нет зарезервированного товара");
        }

        // Порядок обхода: зона, ряд, x, y, z — как ячейки стоят на складе
        Map<Integer, LocationEntity> locations = new HashMap<>();
        Map<Integer, String> zoneCodes = new HashMap<>();
        for (StockReservationEntity r : reservations) {
            locations.computeIfAbsent(r.getLocationId(), id -> locationRepository.findById(id).orElse(null));
        }
        for (ZoneEntity zone : zoneRepository.findAll()) {
            zoneCodes.put(zone.getZoneId(), zone.getCode() == null ? "" : zone.getCode());
        }
        List<StockReservationEntity> sorted = new ArrayList<>(reservations);
        sorted.sort(Comparator.comparing((StockReservationEntity r) -> walkKey(locations.get(r.getLocationId()), zoneCodes))
                .thenComparingInt(StockReservationEntity::getReservationId));

        PickingRouteEntity route = new PickingRouteEntity();
        route.setOrderId(orderId);
        route.setStatusId(PickingRouteStatus.CREATED.getCode());
        route.setCreatedAt(now());
        route.setDel(false);
        route = routeRepository.save(route);

        int sortOrder = 0;
        for (StockReservationEntity r : sorted) {
            PickingRouteLineEntity line = new PickingRouteLineEntity();
            line.setRouteId(route.getRouteId());
            line.setOrderLineId(r.getOrderLineId());
            line.setProductId(r.getProductId());
            line.setBatchId(r.getBatchId());
            line.setLocationId(r.getLocationId());
            line.setReservationId(r.getReservationId());
            line.setQtyPlanned(r.getQuantity());
            line.setSortOrder(++sortOrder);
            routeLineRepository.save(line);
        }

        order.setStatusId(OrderStatus.PICKING.getCode());
        orderRepository.save(order);
        log.info("Заказ {} передан в сборку: маршрут комплектации id={}, строк {}",
                order.getDocumentNumber(), route.getRouteId(), sorted.size());
        return route.getRouteId();
    }

    @Override
    @Transactional
    public void startRoute(int routeId, int employeeId) {
        PickingRouteEntity route = getRouteEntity(routeId);
        requireStatus(route, PickingRouteStatus.CREATED, "Взять маршрут в работу");
        route.setStatusId(PickingRouteStatus.IN_PROGRESS.getCode());
        route.setEmployeeId(employeeId);
        route.setStartedAt(now());
        routeRepository.save(route);
        log.info("Маршрут комплектации id={} взят в работу, сотрудник {}", routeId, employeeId);
    }

    @Override
    @Transactional
    public boolean confirmLine(int routeLineId, int quantity, int employeeId) {
        PickingRouteLineEntity line = routeLineRepository.findById(routeLineId)
                .orElseThrow(() -> new NotFoundException("Строка маршрута комплектации " + routeLineId + " не найдена"));
        PickingRouteEntity route = getRouteEntity(line.getRouteId());
        requireStatus(route, PickingRouteStatus.IN_PROGRESS, "Подтвердить сборку");
        if (line.isComplete()) {
            throw new WmsException("Строка уже подтверждена");
        }
        if (quantity < 0 || quantity > line.getQtyPlanned()) {
            throw new WmsException("Собранное количество должно быть от 0 до " + line.getQtyPlanned());
        }

        if (quantity > 0) {
            inventoryService.move(line.getBatchId(), line.getProductId(), line.getLocationId(),
                    wmsProperties.getLocations().getShippingId(), quantity, employeeId, MovementType.PICKING);
        }
        // Резерв израсходован: собранное лежит в зоне отгрузки, недостача из резерва снимается
        reservationRepository.findById(line.getReservationId()).ifPresent(reservationRepository::delete);

        orderLineRepository.findById(line.getOrderLineId()).ifPresent(orderLine -> {
            orderLine.setQtyPicked(orderLine.getQtyPicked() + quantity);
            orderLine.setQtyReserved(Math.max(orderLine.getQtyReserved() - (line.getQtyPlanned() - quantity), 0));
            orderLineRepository.save(orderLine);
        });

        line.setQtyPicked(quantity);
        line.setComplete(true);
        routeLineRepository.save(line);
        if (quantity < line.getQtyPlanned()) {
            log.warn("Недостача при сборке: маршрут {}, ячейка {}, партия {}, план {}, собрано {}",
                    route.getRouteId(), line.getLocationId(), line.getBatchId(), line.getQtyPlanned(), quantity);
        }

        boolean allDone = routeLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(route.getRouteId()).stream()
                .allMatch(PickingRouteLineEntity::isComplete);
        if (allDone) {
            route.setStatusId(PickingRouteStatus.COMPLETED.getCode());
            route.setFinishedAt(now());
            routeRepository.save(route);
            orderRepository.findById(route.getOrderId()).ifPresent(order -> {
                order.setStatusId(OrderStatus.PICKED.getCode());
                orderRepository.save(order);
            });
            log.info("Маршрут комплектации id={} завершён, заказ id={} собран", route.getRouteId(), route.getOrderId());
        }
        return allDone;
    }

    @Override
    @Transactional
    public void cancelRoute(int routeId) {
        PickingRouteEntity route = getRouteEntity(routeId);
        PickingRouteStatus status = PickingRouteStatus.fromCode(route.getStatusId());
        if (status != PickingRouteStatus.CREATED && status != PickingRouteStatus.IN_PROGRESS) {
            throw new WmsException("Отменить можно только маршрут в статусе «Создан» или «В работе»");
        }
        boolean anyConfirmed = routeLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(routeId).stream()
                .anyMatch(PickingRouteLineEntity::isComplete);
        if (anyConfirmed) {
            throw new WmsException("Нельзя отменить маршрут: часть строк уже собрана");
        }
        route.setStatusId(PickingRouteStatus.CANCELLED.getCode());
        route.setFinishedAt(now());
        routeRepository.save(route);
        orderRepository.findById(route.getOrderId()).ifPresent(order -> {
            order.setStatusId(OrderStatus.RESERVED.getCode());
            orderRepository.save(order);
        });
        log.info("Маршрут комплектации id={} отменён, заказ id={} снова «Зарезервирован»", routeId, route.getOrderId());
    }

    // ---------- представления ----------

    @Override
    @Transactional(readOnly = true)
    public List<RouteView> getRoutes() {
        List<RouteView> result = new ArrayList<>();
        for (PickingRouteEntity route : routeRepository.findActiveOrderByIdDesc()) {
            result.add(toView(route));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RouteView getRoute(int routeId) {
        return toView(getRouteEntity(routeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findRouteIdByOrder(int orderId) {
        return routeRepository.findByOrderAndStatuses(orderId, List.of(
                        PickingRouteStatus.CREATED.getCode(), PickingRouteStatus.IN_PROGRESS.getCode(),
                        PickingRouteStatus.COMPLETED.getCode()))
                .stream().findFirst().map(PickingRouteEntity::getRouteId);
    }

    private RouteView toView(PickingRouteEntity route) {
        SimpleDateFormat dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
        CustomerOrderEntity order = orderRepository.findById(route.getOrderId()).orElse(null);
        Optional<CustomerEntity> customer = order == null ? Optional.empty()
                : customerRepository.findById(order.getCustomerId());

        List<RouteLineView> lines = new ArrayList<>();
        int planned = 0;
        int picked = 0;
        int completed = 0;
        double weight = 0;
        double volume = 0;
        for (PickingRouteLineEntity line : routeLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(route.getRouteId())) {
            Optional<ProductEntity> product = productRepository.findById(line.getProductId());
            String expiry = batchRepository.findById(line.getBatchId())
                    .map(BatchEntity::getExpiry_date).map(date::format).orElse("");
            lines.add(new RouteLineView(line.getLineId(),
                    product.map(ProductEntity::getProductCode).orElse("?"),
                    product.map(ProductEntity::getProductName).orElse("товар " + line.getProductId()),
                    line.getBatchId(), expiry,
                    locationRepository.findById(line.getLocationId()).map(LocationEntity::getLocationCode)
                            .orElse("ячейка " + line.getLocationId()),
                    line.getQtyPlanned(), line.getQtyPicked(), line.isComplete()));
            planned += line.getQtyPlanned();
            picked += line.getQtyPicked();
            if (line.isComplete()) completed++;
            weight += line.getQtyPlanned() * product.map(ProductEntity::getWeight).map(Float::doubleValue).orElse(0.0);
            volume += line.getQtyPlanned() * product.map(ProductEntity::getVolume).map(Float::doubleValue).orElse(0.0);
        }
        String employee = route.getEmployeeId() > 0
                ? employeeRepository.findById(route.getEmployeeId()).map(e -> e.getEmployeeName()).orElse("")
                : "";
        PickingRouteStatus status = PickingRouteStatus.fromCode(route.getStatusId());
        return new RouteView(route.getRouteId(), route.getOrderId(),
                order != null ? order.getDocumentNumber() : "",
                order != null && order.getOrderDate() != null ? date.format(order.getOrderDate()) : "",
                order != null && order.getShipDate() != null ? date.format(order.getShipDate()) : "",
                customer.map(c -> c.getCustomerName() + " (" + c.getCustomerCode() + ")").orElse(""),
                customer.map(CustomerEntity::getAddress).orElse(""),
                order != null && order.getNote() != null ? order.getNote() : "",
                route.getCreatedAt() != null ? dateTime.format(route.getCreatedAt()) : "",
                status.getCode(), status.getTitle(), employee,
                planned, picked, completed,
                Math.round(weight * 100) / 100.0, Math.round(volume * 100) / 100.0,
                lines);
    }

    // ---------- вспомогательные ----------

    /** Ключ сортировки ячейки для обхода склада. */
    static String walkKey(LocationEntity location, Map<Integer, String> zoneCodes) {
        if (location == null) return "~";
        return String.format("%s|%s|%05d|%05d|%05d",
                zoneCodes.getOrDefault(location.getZoneID(), ""),
                location.getRow() == null ? "" : location.getRow(),
                location.getX(), location.getY(), location.getZ());
    }

    private CustomerOrderEntity getOrderEntity(int orderId) {
        return orderRepository.findById(orderId)
                .filter(o -> !Boolean.TRUE.equals(o.getDel()))
                .orElseThrow(() -> new NotFoundException("Заказ " + orderId + " не найден"));
    }

    private PickingRouteEntity getRouteEntity(int routeId) {
        return routeRepository.findById(routeId)
                .filter(r -> !Boolean.TRUE.equals(r.getDel()))
                .orElseThrow(() -> new NotFoundException("Маршрут комплектации " + routeId + " не найден"));
    }

    private static void requireStatus(PickingRouteEntity route, PickingRouteStatus expected, String action) {
        PickingRouteStatus actual = PickingRouteStatus.fromCode(route.getStatusId());
        if (actual != expected) {
            throw new WmsException(action + " можно только для маршрута в статусе «" + expected.getTitle()
                    + "», текущий статус: «" + actual.getTitle() + "»");
        }
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
