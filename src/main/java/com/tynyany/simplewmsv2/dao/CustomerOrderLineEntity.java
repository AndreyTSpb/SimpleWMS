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
 * Строка заказа клиента.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_order_line")
public class CustomerOrderLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int lineId;
    private int orderId;
    private int productId;
    private int qtyOrdered;
    private int qtyReserved;
    private int qtyPicked;
    private int qtyShipped;
}
