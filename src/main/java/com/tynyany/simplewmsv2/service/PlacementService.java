package com.tynyany.simplewmsv2.service;

import java.util.List;

/**
 * Размещение принятого товара из зоны приёмки в ячейки хранения.
 */
public interface PlacementService {

    /**
     * @param routesCreated количество созданных маршрутов (по одному на зону хранения)
     * @param linesCreated  количество строк во всех маршрутах
     */
    record GenerationResult(int routesCreated, int linesCreated) {}

    /**
     * Формирует маршруты размещения для всего принятого, но ещё не размещённого товара.
     * Строки прихода, попавшие в маршрут, отмечаются, чтобы не попасть в маршрут повторно.
     */
    GenerationResult generateRoutes();

    /** Строка маршрута для отображения. */
    record RouteLineView(String productCode, String productName, int batchId,
                         String locationCode, int quantity, boolean complete) {}

    /** Маршрут размещения для отображения. */
    record RouteView(int id, String created, String zone, String status,
                     int totalQuantity, List<RouteLineView> lines) {}

    /** Маршруты размещения, новые сверху. */
    List<RouteView> getRoutes();
}
