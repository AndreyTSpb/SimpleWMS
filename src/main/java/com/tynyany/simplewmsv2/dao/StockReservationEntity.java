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
 * Резерв остатка под строку заказа: сколько единиц партии в конкретной ячейке закреплено за заказом.
 * Свободный остаток партии в ячейке = stocks.quantity − сумма резервов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_reservation")
public class StockReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reservationId;
    private int orderId;
    private int orderLineId;
    private int productId;
    private int batchId;
    private int locationId;
    private int quantity;
}
