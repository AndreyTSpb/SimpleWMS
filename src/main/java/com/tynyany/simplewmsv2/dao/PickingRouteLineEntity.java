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
 * Строка маршрута комплектации: взять партию из ячейки (одна строка на один резерв заказа).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "picking_route_line")
public class PickingRouteLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int lineId;
    private int routeId;
    private int orderLineId;
    private int productId;
    private int batchId;
    private int locationId;
    /** Резерв, из которого создана строка; удаляется при подтверждении. */
    private int reservationId;
    private int qtyPlanned;
    private int qtyPicked;
    private boolean complete;
    /** Порядок обхода ячеек. */
    private int sortOrder;
}
