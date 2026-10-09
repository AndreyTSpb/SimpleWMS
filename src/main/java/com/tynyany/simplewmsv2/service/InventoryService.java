package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.domain.MovementType;

/**
 * Учёт остатков: каждое изменение остатка в stocks сопровождается записью в журнале stock_movements.
 */
public interface InventoryService {

    /**
     * Поступление товара на склад (приёмка): остаток появляется в ячейке toLocationId.
     */
    void receive(int batchId, int productId, int toLocationId, int quantity, int employeeId);

    /**
     * Перемещение партии между ячейками (размещение, перемещение, сборка).
     *
     * @throws com.tynyany.simplewmsv2.exception.WmsException если в исходной ячейке недостаточно остатка
     */
    void move(int batchId, int productId, int fromLocationId, int toLocationId,
              int quantity, int employeeId, MovementType type);

    /**
     * Списание партии из ячейки (корректировка, отгрузка): остаток уменьшается, в журнале «куда» = 0.
     *
     * @throws com.tynyany.simplewmsv2.exception.WmsException если в ячейке недостаточно остатка
     */
    void writeOff(int batchId, int fromLocationId, int quantity, int employeeId, MovementType type);

    /**
     * Текущий остаток партии в ячейке (0, если записи нет).
     */
    int getQuantity(int batchId, int locationId);
}
