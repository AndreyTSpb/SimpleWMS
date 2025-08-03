package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockMovementEntity;
import org.springframework.data.repository.CrudRepository;

public interface StockMovementRepository extends CrudRepository<StockMovementEntity, Integer> {
}
