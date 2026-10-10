package com.tynyany.simplewmsv2.service;

import java.util.List;
import java.util.Optional;

/**
 * Комплектация (сборка) заказов: товар из ячеек хранения переносится в зону отгрузки по резервам заказа.
 */
public interface PickingService {

    /**
     * Передача полностью зарезервированного заказа в сборку: создаётся маршрут комплектации
     * (по строке на каждый резерв, в порядке обхода ячеек), заказ переходит в статус «В сборке».
     *
     * @return id маршрута
     */
    int createRoute(int orderId);

    /** Кладовщик берёт маршрут в работу: «Создан» → «В работе». */
    void startRoute(int routeId, int employeeId);

    /**
     * Подтверждение строки: собранное количество перемещается из ячейки в зону отгрузки (движение «Сборка»),
     * резерв строки снимается. Если собрано меньше плана — разница фиксируется как недостача.
     *
     * @param quantity фактически собранное количество, от 0 до планового
     * @return true, если это была последняя строка: маршрут завершён, заказ «Собран»
     */
    boolean confirmLine(int routeLineId, int quantity, int employeeId);

    /** Отмена маршрута, пока ни одна строка не подтверждена: заказ возвращается в «Зарезервирован». */
    void cancelRoute(int routeId);

    // ---------- представления ----------

    record RouteLineView(int lineId, String productCode, String productName, int batchId, String expiryDate,
                         String locationCode, int planned, int picked, boolean complete) {
        public int shortage() {
            return complete ? Math.max(planned - picked, 0) : 0;
        }
    }

    record RouteView(int id, int orderId, String orderNumber, String orderDate, String shipDate,
                     String customer, String address, String note, String created,
                     int statusCode, String status, String employee,
                     int totalPlanned, int totalPicked, int completedLines,
                     double weight, double volume, List<RouteLineView> lines) {
        public int shortage() {
            return lines.stream().mapToInt(RouteLineView::shortage).sum();
        }
    }

    /** Маршруты комплектации, новые сверху. */
    List<RouteView> getRoutes();

    RouteView getRoute(int routeId);

    /** Последний не отменённый маршрут заказа (для ссылки из карточки заказа). */
    Optional<Integer> findRouteIdByOrder(int orderId);
}
