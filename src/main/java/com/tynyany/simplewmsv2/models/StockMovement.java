package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.Stock;
import com.tynyany.simplewmsv2.service.StockMovementService;
import com.tynyany.simplewmsv2.service.StockService;

/**
 * Фиксация перемещения товара между зонами хранения
 */
public class StockMovement {

    private final StockService stockService;
    private final int batchId;
    private final int productId;
    private final int fromLocationId;
    private final int toLocationId;
    private final int quantity;

    public StockMovement(int batchId, int fromLocationId, int toLocationId, int quantity, int productId, int employerId, int movementTypeId, int shipingItemId, StockMovementService stockMovementService, StockService stockService) {
        this.stockService = stockService;

        this.batchId = batchId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.quantity = quantity;
        this.productId = productId;
        if(fromLocationId > 0) updFromStock(); //если "ноль" то товар только прибыл на склад
        updToStock();
        new AddStockMovement(batchId, fromLocationId, toLocationId, quantity, employerId, movementTypeId, shipingItemId, stockMovementService);
    }

    private void updFromStock(){
        // 1. Обновляем Stocks (уменьшаем в fromLocationId)
        Stock fromStock = stockService.getByBatchIdAndFromLocationId(this.batchId, this.fromLocationId, false);
        fromStock.setQuantity(fromStock.getQuantity() - this.quantity);
        stockService.update(fromStock);
    }

    private void updToStock(){
        // 2. Обновляем Stocks (увеличиваем в toLocationId)
        Stock toStock = stockService.getByBatchIdAndFromLocationId(this.batchId, this.toLocationId, false);
        if(toStock == null){
            Stock toStockNew = new Stock(
                    0,
                    this.productId,
                    this.batchId,
                    this.toLocationId,
                    this.quantity,
                    false
            );
            stockService.add(toStockNew);
        }else{
            toStock.setQuantity(toStock.getQuantity()+this.quantity);
            stockService.update(toStock);
        }
    }

}
