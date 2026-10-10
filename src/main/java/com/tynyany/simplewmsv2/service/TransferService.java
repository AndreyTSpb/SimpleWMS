package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dto.StockPositionRow;

import java.util.List;

/**
 * Перемещение товара между ячейками хранения и поиск остатков.
 */
public interface TransferService {

    /**
     * @param overCapacity после перемещения занятый объём ячейки-получателя превышает её вместимость
     * @param targetCode   код ячейки-получателя
     */
    record TransferResult(boolean overCapacity, String targetCode, double usedVolume, double capacity) {}

    /**
     * Перемещение партии (целиком или части) из одной ячейки хранения в другую — движение «Перемещение».
     * Нехватка места в ячейке-получателе не блокирует операцию, а возвращается как предупреждение.
     */
    TransferResult transfer(int batchId, int fromLocationId, int toLocationId, int quantity, int employeeId);

    /** Содержимое ячейки. */
    List<StockPositionRow> cellContents(int locationId);

    /** Где лежит товар: поиск по коду, наименованию или штрихкоду. */
    List<StockPositionRow> findProduct(String query);

    /** Запись журнала перемещений для отображения. */
    record MoveView(String date, String productCode, String productName, int batchId,
                    String fromCode, String toCode, int quantity, String employee) {}

    /** Последние перемещения. */
    List<MoveView> recentMoves();
}
