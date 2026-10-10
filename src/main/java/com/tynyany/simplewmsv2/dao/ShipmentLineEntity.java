package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Строка отгрузки: сколько единиц партии списано из зоны отгрузки по строке заказа.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipment_line")
public class ShipmentLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int lineId;
    private int shipmentId;
    private int orderLineId;
    private int productId;
    private int batchId;
    private int quantity;
}
