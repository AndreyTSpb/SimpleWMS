package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.StockMovementEntity;
import com.tynyany.simplewmsv2.entity.StockMovement;
import com.tynyany.simplewmsv2.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultStockMovementService implements StockMovementService {
    final StockMovementRepository stockMovementRepository;

    @Override
    public StockMovement getById(int stockMovementId) {
        StockMovementEntity stockMovementEntity = stockMovementRepository.findById(stockMovementId).orElse(null);
        if (stockMovementEntity == null) return null;
        return new StockMovement(
                stockMovementEntity.getMovementId(),
                stockMovementEntity.getBatchId(),
                stockMovementEntity.getFromLocationId(),
                stockMovementEntity.getToLocationId(),
                stockMovementEntity.getQuantity(),
                stockMovementEntity.getMovementDate(),
                stockMovementEntity.getEmployeeID(),
                stockMovementEntity.getMovementTypeId(),
                stockMovementEntity.getShipmentItemId(),
                stockMovementEntity.getDeleted()
        );
    }

    @Override
    public List<StockMovement> getAll() {
        Iterable<StockMovementEntity> stockMovementEntities = stockMovementRepository.findAll();
        List<StockMovement> stockMovements = new ArrayList<>();
        for (StockMovementEntity stockMovementEntity : stockMovementEntities) {
            stockMovements.add(new StockMovement(
                    stockMovementEntity.getMovementId(),
                    stockMovementEntity.getBatchId(),
                    stockMovementEntity.getFromLocationId(),
                    stockMovementEntity.getToLocationId(),
                    stockMovementEntity.getQuantity(),
                    stockMovementEntity.getMovementDate(),
                    stockMovementEntity.getEmployeeID(),
                    stockMovementEntity.getMovementTypeId(),
                    stockMovementEntity.getShipmentItemId(),
                    stockMovementEntity.getDeleted()
                    )
            );
        }
        return stockMovements;
    }

    @Override
    public void add(StockMovement stockMovement) {
        StockMovementEntity stockMovementEntity = new StockMovementEntity(
                0,
                stockMovement.getBatchId(),
                stockMovement.getFromLocationId(),
                stockMovement.getToLocationId(),
                stockMovement.getQuantity(),
                stockMovement.getMovementDate(),
                stockMovement.getEmployeeID(),
                stockMovement.getMovementTypeId(),
                stockMovement.getShipmentItemId(),
                stockMovement.getDel()
        );
        stockMovementRepository.save(stockMovementEntity);
    }

    @Override
    public void update(StockMovement stockMovement) {
        StockMovementEntity stockMovementEntity = new StockMovementEntity(
                stockMovement.getMovementId(),
                stockMovement.getBatchId(),
                stockMovement.getFromLocationId(),
                stockMovement.getToLocationId(),
                stockMovement.getQuantity(),
                stockMovement.getMovementDate(),
                stockMovement.getEmployeeID(),
                stockMovement.getMovementTypeId(),
                stockMovement.getShipmentItemId(),
                stockMovement.getDel()
        );
        stockMovementRepository.save(stockMovementEntity);
    }

    @Override
    public void delete(int stockMovementId) {
        StockMovement stockMovement = getById(stockMovementId);
        StockMovementEntity stockMovementEntity = new StockMovementEntity(
                stockMovement.getMovementId(),
                stockMovement.getBatchId(),
                stockMovement.getFromLocationId(),
                stockMovement.getToLocationId(),
                stockMovement.getQuantity(),
                stockMovement.getMovementDate(),
                stockMovement.getEmployeeID(),
                stockMovement.getMovementTypeId(),
                stockMovement.getShipmentItemId(),
                true
        );
        stockMovementRepository.save(stockMovementEntity);
    }
}
