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
 * Маршрут комплектации: сборка одного заказа клиента из ячеек хранения в зону отгрузки.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "picking_route")
public class PickingRouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int routeId;
    private int orderId;
    /** Код статуса — {@link com.tynyany.simplewmsv2.domain.PickingRouteStatus}. */
    private int statusId;
    /** Сотрудник, взявший маршрут в работу (0 — не назначен). */
    private int employeeId;
    private Timestamp createdAt;
    private Timestamp startedAt;
    private Timestamp finishedAt;
    private Boolean del = false;
}
