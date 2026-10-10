package com.tynyany.simplewmsv2.dto;

import java.sql.Timestamp;

/**
 * Свободный остаток партии в ячейке (остаток минус резервы) — кандидат для резервирования.
 */
public interface AvailableStockRow {
    Integer getBatchId();
    Integer getLocationId();
    Integer getAvailable();
    Timestamp getExpiryDate();
}
