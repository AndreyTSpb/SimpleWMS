package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockMovementEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StockMovementRepository extends CrudRepository<StockMovementEntity, Integer> {

    /** Последние движения указанного типа. */
    List<StockMovementEntity> findTop30ByMovementTypeIdOrderByMovementIdDesc(int movementTypeId);
}
