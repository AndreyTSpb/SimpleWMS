package com.tynyany.simplewmsv2.dto;

import java.sql.Timestamp;

/**
 * Позиция остатка: партия товара в ячейке (см. StockRepository.findPositionsInLocation / findPositionsByProduct).
 */
public interface StockPositionRow {
    Integer getStockId();
    Integer getBatchId();
    Integer getProductId();
    String getProductCode();
    String getProductName();
    Timestamp getExpiryDate();
    Integer getQuantity();
    Integer getLocationId();
    String getLocationCode();
    /** Зарезервировано под заказы. */
    Integer getReserved();

    default int getFree() {
        return Math.max(getQuantity() - (getReserved() == null ? 0 : getReserved()), 0);
    }
}
