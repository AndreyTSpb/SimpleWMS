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
 * Заказ клиента (заголовок).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_order")
public class CustomerOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;
    /** Номер заказа во внешней системе (ERP) или присвоенный при ручном создании. */
    private String documentNumber;
    private int customerId;
    private Timestamp orderDate;
    /** Плановая дата отгрузки. */
    private Timestamp shipDate;
    /** Код статуса — {@link com.tynyany.simplewmsv2.domain.OrderStatus}. */
    private int statusId;
    private String note;
    private Timestamp createdAt;
    private Boolean del = false;
}
