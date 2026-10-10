package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.OrderJSON;

import java.util.List;

/**
 * Заказы клиентов и резервирование остатков под них.
 */
public interface OrderService {

    /**
     * @param orderId             id созданного заказа
     * @param linesCreated        создано строк
     * @param missingProductCodes коды товаров, которых нет в справочнике (строки пропущены)
     * @param reserve             результат резервирования
     */
    record ErpOrderResult(int orderId, int linesCreated, List<String> missingProductCodes, ReserveResult reserve) {}

    /**
     * @param reservedNow зарезервировано единиц этим вызовом
     * @param shortage    не хватило единиц по всем строкам
     * @param status      статус заказа после резервирования
     */
    record ReserveResult(int reservedNow, int shortage, String status) {}

    /** Загрузка заказа из ERP с немедленным резервированием. */
    ErpOrderResult createFromErp(OrderJSON json);

    /** Ручное создание заказа (статус «Новый», строки добавляются отдельно). Пустой номер — присваивается автоматически. */
    int create(int customerId, String documentNumber, String shipDate, String note);

    /** Добавление строки в новый заказ (тот же товар — количество суммируется). */
    void addLine(int orderId, int productId, int quantity);

    /** Удаление строки нового заказа. */
    void removeLine(int orderLineId);

    /**
     * Резервирование недостающего количества по всем строкам по правилу FEFO.
     * Повторный вызов добирает то, чего не хватило ранее.
     */
    ReserveResult reserve(int orderId);

    /** Отмена заказа до передачи в сборку: резервы снимаются. */
    void cancel(int orderId);

    // ---------- представления для страниц ----------

    record ReservationView(String locationCode, int batchId, String expiryDate, int quantity) {}

    record LineView(int lineId, String productCode, String productName, int ordered, int reserved,
                    int picked, int shipped, List<ReservationView> reservations) {
        public int shortage() {
            return Math.max(ordered - reserved, 0);
        }
    }

    record OrderView(int id, String documentNumber, String customer, String orderDate, String shipDate,
                     int statusCode, String status, boolean editable, String note,
                     int totalOrdered, int totalReserved, List<LineView> lines) {}

    /** Заказы, новые сверху; statusCode = null — все. */
    List<OrderView> getOrders(Integer statusCode);

    OrderView getOrder(int orderId);
}
