package com.tynyany.simplewmsv2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Отгрузка собранных заказов клиентам и выгрузка результатов в ERP.
 */
public interface ShippingService {

    /**
     * Отгрузка собранного заказа: по фактически собранным строкам маршрута сборки товар списывается
     * из ячейки отгрузки (движение «Отгрузка»), в строках заказа заполняется «отгружено», заказ — «Отгружен».
     *
     * @return id документа отгрузки
     */
    int ship(int orderId, int employeeId);

    /** ERP подтверждает получение отгрузки (повторное подтверждение не меняет дату). */
    void confirmExport(int shipmentId);

    // ---------- представления ----------

    record LineView(String productCode, String productName, int batchId, int quantity, double weight) {}

    record ShipmentView(int id, String number, int orderId, String orderNumber, String customer, String customerCode,
                        String address, String shipped, String employee, String exported,
                        int totalQuantity, double totalWeight, List<LineView> lines) {}

    /** Отгрузки, новые сверху. */
    List<ShipmentView> getShipments();

    ShipmentView getShipment(int shipmentId);

    /** Отгрузка заказа, если он уже отгружен. */
    Optional<Integer> findShipmentIdByOrder(int orderId);

    // ---------- выгрузка в ERP ----------

    record ErpShipmentLine(String code, int qnt) {}

    /**
     * @param confirmed ERP уже подтвердила получение
     * @param body      строки, сгруппированные по товару
     */
    record ErpShipment(int id, String number, String orderERP, String customerCode, String shipDate,
                       boolean confirmed, List<ErpShipmentLine> body) {}

    /** Отгрузки начиная с указанной даты (включительно), в порядке проведения. */
    List<ErpShipment> getForErp(LocalDate from);
}
