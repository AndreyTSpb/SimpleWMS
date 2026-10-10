package com.tynyany.simplewmsv2.it;

import com.tynyany.simplewmsv2.dto.AvailableStockRow;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import com.tynyany.simplewmsv2.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Собственные SQL-запросы (native query и отчёты) на настоящей PostgreSQL.
 * Данные: отдельный товар с тремя партиями в ячейках хранения — истёкший срок, срок через 10 дней, без срока.
 */
class RepositoryQueriesIT extends IntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private StockReservationRepository reservationRepository;
    @Autowired private ReportService reportService;

    private int productId;
    private int expiredBatch;
    private int soonBatch;
    private int noExpiryBatch;

    @BeforeEach
    void stock() {
        Integer existing = jdbc.query("SELECT productid FROM product WHERE product_code = 'IT-FEFO'",
                rs -> rs.next() ? rs.getInt(1) : null);
        if (existing != null) {
            productId = existing;
            List<Integer> batches = jdbc.queryForList("SELECT batch_id FROM batches WHERE product_id = ? ORDER BY batch_id",
                    Integer.class, productId);
            expiredBatch = batches.get(0);
            soonBatch = batches.get(1);
            noExpiryBatch = batches.get(2);
            return;
        }
        productId = jdbc.queryForObject("INSERT INTO product (product_name, product_code, description, weight, volume,"
                + " expiration_date_required, del, supplierid, categoryid, ext_bar_code, int_bar_code, abcid)"
                + " VALUES ('Клей тестовый', 'IT-FEFO', '', 0.1, 1.0, true, false, 1, 3, 'IT-EXT-FEFO', '', 2)"
                + " RETURNING productid", Integer.class);
        LocalDate today = LocalDate.now();
        expiredBatch = batch(today.minusDays(5), today.minusDays(60));
        soonBatch = batch(today.plusDays(10), today.minusDays(30));
        noExpiryBatch = batch(null, today.minusDays(90));
        stock(expiredBatch, 211, 5);   // SC2
        stock(soonBatch, 105, 20);     // SC1
        stock(noExpiryBatch, 106, 40); // SC1
        stock(soonBatch, 2, 3);        // зона отгрузки — не резервируется
    }

    @Test
    void fefoOrdersByExpirySkipsExpiredAndSystemCells() {
        List<AvailableStockRow> rows = reservationRepository.findAvailableFefo(productId, List.of(1, 2));
        assertThat(rows).extracting(AvailableStockRow::getBatchId).containsExactly(soonBatch, noExpiryBatch);
        assertThat(rows).extracting(AvailableStockRow::getLocationId).doesNotContain(2);
        assertThat(rows).extracting(AvailableStockRow::getAvailable).containsExactly(20, 40);
    }

    @Test
    void stockByBatchMarksExpiryStatesAndFiltersExpiring() {
        List<ReportService.BatchStockRow> all = reportService.stockByBatch(new ReportService.StockFilter("IT-FEFO", null, "", false));
        assertThat(all).hasSize(4);
        assertThat(all).filteredOn(r -> r.batchId() == expiredBatch)
                .extracting(ReportService.BatchStockRow::expiryState).containsOnly(ReportService.ExpiryState.EXPIRED);
        assertThat(all).filteredOn(r -> r.batchId() == soonBatch)
                .extracting(ReportService.BatchStockRow::expiryState).containsOnly(ReportService.ExpiryState.SOON);
        assertThat(all).filteredOn(r -> r.batchId() == noExpiryBatch)
                .extracting(ReportService.BatchStockRow::expiryState).containsOnly(ReportService.ExpiryState.NONE);

        List<ReportService.BatchStockRow> expiring = reportService.stockByBatch(
                new ReportService.StockFilter("клей тестовый", null, "", true));
        assertThat(expiring).extracting(ReportService.BatchStockRow::batchId).doesNotContain(noExpiryBatch);

        // поиск по штрихкоду и фильтр по ячейке
        assertThat(reportService.stockByBatch(new ReportService.StockFilter("IT-EXT-FEFO", null, "SC1-01-03", false)))
                .extracting(ReportService.BatchStockRow::batchId).containsExactlyInAnyOrder(soonBatch, noExpiryBatch);
    }

    @Test
    void stockByProductSeparatesShippingZone() {
        ReportService.ProductStockRow row = reportService.stockByProduct(new ReportService.StockFilter("IT-FEFO", null, "", false)).get(0);
        assertThat(row.total()).isEqualTo(68);
        assertThat(row.storage()).isEqualTo(65);
        assertThat(row.shipping()).isEqualTo(3);
        assertThat(row.worstExpiry()).isEqualTo(ReportService.ExpiryState.EXPIRED);
    }

    @Test
    void cellLoadCountsVolumeAndSkipsSystemCells() {
        List<ReportService.CellLoadRow> cells = reportService.cellLoad(new ReportService.StockFilter("", null, "SC1-01-03-1", false));
        assertThat(cells).hasSize(1);
        assertThat(cells.get(0).used()).isGreaterThanOrEqualTo(20.0);
        assertThat(reportService.cellLoad(new ReportService.StockFilter("", null, "", false)))
                .extracting(ReportService.CellLoadRow::locationCode)
                .doesNotContain("PRM-01-01-1-1", "OTG-01-01-1-1");
    }

    @Test
    void dashboardCountsExpiredAndExpiringBatches() {
        ReportService.Dashboard dashboard = reportService.dashboard();
        assertThat(dashboard.expiredBatches()).isGreaterThanOrEqualTo(1);
        assertThat(dashboard.expiringBatches()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void movementJournalPagesAndFilters() {
        ReportService.MovementPage page = reportService.movements(
                new ReportService.MovementFilter(LocalDate.now().minusDays(1), LocalDate.now(), null, "", null), 1);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.rows().size()).isLessThanOrEqualTo(50);
        ReportService.MovementPage none = reportService.movements(
                new ReportService.MovementFilter(null, null, null, "", Integer.MAX_VALUE), 5);
        assertThat(none.total()).isZero();
        assertThat(none.page()).isEqualTo(1);
    }

    private int batch(LocalDate expiry, LocalDate received) {
        return jdbc.queryForObject("INSERT INTO batches (product_id, supplier_id, date_received, quantity_received,"
                        + " expiry_date, receiving_id, del) VALUES (?, 1, ?, 100, ?, 0, false) RETURNING batch_id", Integer.class,
                productId, Timestamp.valueOf(received.atStartOfDay()),
                expiry == null ? null : Timestamp.valueOf(expiry.atStartOfDay()));
    }

    private void stock(int batchId, int locationId, int quantity) {
        jdbc.update("INSERT INTO stocks (batch_id, product_id, location_id, quantity, del) VALUES (?, ?, ?, ?, false)",
                batchId, productId, locationId, quantity);
    }
}
