package com.tynyany.simplewmsv2.service;

import java.util.List;

/**
 * Размещение принятого товара из зоны приёмки в ячейки хранения.
 */
public interface PlacementService {

    /**
     * @param routesCreated количество созданных маршрутов (по одному на зону хранения)
     * @param linesCreated  количество строк во всех маршрутах
     * @param linesSkipped  строки, для которых не нашлось ячейки (остаются в зоне приёмки)
     */
    record GenerationResult(int routesCreated, int linesCreated, int linesSkipped) {}

    /**
     * Формирует маршруты размещения для всего принятого, но ещё не размещённого товара.
     * Ячейка берётся из строки прихода, если её выбрали при приёмке, иначе подбирается автоматически
     * ({@link PlacementCellPicker}). Строки прихода, попавшие в маршрут, отмечаются.
     */
    GenerationResult generateRoutes();

    /** Кладовщик берёт маршрут в работу: «Создан» → «В работе». */
    void startRoute(int routeId, int employeeId);

    /**
     * Подтверждение строки маршрута: товар перемещается из зоны приёмки в ячейку (движение «Размещение»).
     *
     * @param actualLocationId фактическая ячейка, если товар положен не туда, куда планировалось; 0 — плановая
     * @return true, если это была последняя строка и маршрут завершён
     */
    boolean confirmLine(int routeLineId, int actualLocationId, int employeeId);

    /**
     * Отмена маршрута, пока ни одна строка не подтверждена. Товар остаётся в зоне приёмки
     * и снова попадёт в следующий сформированный маршрут.
     */
    void cancelRoute(int routeId);

    /** Строка маршрута для отображения. */
    record RouteLineView(int lineId, String productCode, String productName, int batchId,
                         int locationId, String locationCode, int quantity, boolean complete) {}

    /** Маршрут размещения для отображения. */
    record RouteView(int id, String created, String zone, int statusCode, String status, String employee,
                     int totalQuantity, int completedLines, List<RouteLineView> lines) {}

    /** Маршруты размещения, новые сверху. */
    List<RouteView> getRoutes();

    /** Один маршрут (для печати листа размещения). */
    RouteView getRoute(int routeId);
}
