package com.tynyany.simplewmsv2.service;

import java.time.LocalDate;
import java.util.List;

/**
 * Отчёты: состояние склада, журнал движений, сводки за период, показатели главной страницы.
 */
public interface ReportService {

    // ---------- состояние склада ----------

    /**
     * @param product      код, часть названия или штрихкод товара (пусто — все)
     * @param zoneId       зона хранения (null — все)
     * @param location     часть кода ячейки (пусто — все)
     * @param expiringOnly только партии с истёкшим или истекающим сроком годности
     */
    record StockFilter(String product, Integer zoneId, String location, boolean expiringOnly) {}

    /** Состояние срока годности партии. */
    enum ExpiryState { NONE, OK, SOON, EXPIRED }

    record BatchStockRow(String productCode, String productName, int batchId, String expiryDate, ExpiryState expiryState,
                         int locationId, String locationCode, String zone, int quantity, int reserved) {
        public int free() {
            return Math.max(quantity - reserved, 0);
        }
    }

    /**
     * @param storage   в ячейках хранения
     * @param reserved  из них в резерве под заказы
     * @param receiving в зоне приёмки (ещё не размещено)
     * @param shipping  в зоне отгрузки (собрано под заказы)
     */
    record ProductStockRow(String productCode, String productName, int total, int storage, int reserved,
                           int receiving, int shipping, ExpiryState worstExpiry) {
        public int free() {
            return Math.max(storage - reserved, 0);
        }
    }

    record CellLoadRow(String locationCode, String zone, double capacity, double used, int positions) {
        public int percent() {
            return capacity > 0 ? (int) Math.round(used * 100 / capacity) : 0;
        }
    }

    List<BatchStockRow> stockByBatch(StockFilter filter);

    List<ProductStockRow> stockByProduct(StockFilter filter);

    List<CellLoadRow> cellLoad(StockFilter filter);

    // ---------- журнал движений ----------

    record MovementFilter(LocalDate from, LocalDate to, Integer type, String product, Integer batchId) {}

    record MovementRow(String date, String type, String productCode, String productName, int batchId,
                       String fromLocation, String toLocation, int quantity, String employee) {}

    record MovementPage(List<MovementRow> rows, int page, int pages, long total) {}

    MovementPage movements(MovementFilter filter, int page);

    // ---------- сводки за период ----------

    record ShipmentByCustomer(String customer, int shipments, int quantity, double weight) {}

    record ShipmentByProduct(String productCode, String productName, int ordered, int shipped) {
        public int shortage() {
            return Math.max(ordered - shipped, 0);
        }
    }

    record ShipmentReport(List<ShipmentByCustomer> customers, List<ShipmentByProduct> products,
                          int shipments, int quantity, double weight) {}

    ShipmentReport shipmentReport(LocalDate from, LocalDate to);

    record IncomeBySupplier(String supplier, int documents, int lines, int expected, int received) {
        public int difference() {
            return received - expected;
        }
    }

    record IncomeByProduct(String productCode, String productName, int expected, int received) {
        public int difference() {
            return received - expected;
        }
    }

    record IncomeReport(List<IncomeBySupplier> suppliers, List<IncomeByProduct> products,
                        int documents, int expected, int received) {}

    IncomeReport incomeReport(LocalDate from, LocalDate to);

    /**
     * Работа сотрудника за период: сборка (завершённые маршруты комплектации) и размещение.
     */
    record EmployeeWorkRow(String employee, int pickingRoutes, int pickingLines, int pickedQuantity,
                           int shortageLines, int shortageQuantity, int placementRoutes, int placementLines,
                           int placedQuantity) {}

    List<EmployeeWorkRow> employeeWork(LocalDate from, LocalDate to);

    // ---------- главная страница ----------

    record Dashboard(int receivingsWaiting, int receivingsInProgress, int unplacedLines, int openPlacementRoutes,
                     int ordersToReserve, int ordersToPick, int ordersPicking, int ordersToShip,
                     int openPickingRoutes, int expiringBatches, int expiredBatches) {}

    Dashboard dashboard();
}
