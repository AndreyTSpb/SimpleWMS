package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.StockMovement;
import com.tynyany.simplewmsv2.service.StockMovementService;

import java.sql.Timestamp;
/**
 *  Создается запись в StockMovements с указанием to_location_id (зона приемки), типом Receiving и поступившим количеством. from_location_id = NULL.
 */
public class AddStockMovement {

    public AddStockMovement(int batchId, int fromLocationId, int toLocationId, int qntFact, int employerId, int movementTypeId, int shipmentItemId, StockMovementService stockMovementService){
        StockMovement stockMovement = new StockMovement(
                0,
                batchId,
                fromLocationId,
                toLocationId,
                qntFact,
                new Timestamp(System.currentTimeMillis()),
                employerId,
                movementTypeId,
                shipmentItemId,
                false
        );
        assert stockMovementService != null;
        stockMovementService.add(stockMovement);
    }
}
