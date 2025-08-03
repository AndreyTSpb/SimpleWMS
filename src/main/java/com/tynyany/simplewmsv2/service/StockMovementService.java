package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.StockMovement;

import java.util.List;

public interface StockMovementService {
    StockMovement getById(int StockMovementId);
    List<StockMovement> getAll();
    void add(StockMovement stockMovement);
    void update(StockMovement stockMovement);
    void delete(int stockMovementId);
}
