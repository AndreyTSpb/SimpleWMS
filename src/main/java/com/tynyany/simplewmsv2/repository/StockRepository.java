package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockEntity;
import org.springframework.data.repository.CrudRepository;

public interface StockRepository extends CrudRepository<StockEntity, Integer> {
    StockEntity findByBatchIdAndLocationId(int batchId, int locationid);
    StockEntity findByBatchIdAndLocationIdAndDel(int batchId, int locationid, Boolean del);
}
