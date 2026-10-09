package com.tynyany.simplewmsv2.dto;

/**
 * Строка выборки остатков, ожидающих размещения (см. StockRepository.findUnplacedInZone).
 */
public interface UnplacedStockRow {
    Integer getStockId();
    Integer getBatchId();
    Integer getProductId();
    Integer getQuantity();
    /** Ячейка хранения, указанная при приёмке строки. */
    Integer getTargetLocationId();
    Integer getReceivingLineId();
}
