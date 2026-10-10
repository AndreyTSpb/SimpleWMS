package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Документ отгрузки собранного заказа клиенту. Создаётся уже проведённым.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipment")
public class ShipmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int shipmentId;
    /** Номер документа: ОТГ-&lt;id&gt;. */
    private String documentNumber;
    private int orderId;
    private int employeeId;
    private Timestamp shippedAt;
    /** Когда ERP подтвердила получение отгрузки (null — ещё не подтверждала). */
    private Timestamp exportedAt;
    private Boolean del = false;
}
