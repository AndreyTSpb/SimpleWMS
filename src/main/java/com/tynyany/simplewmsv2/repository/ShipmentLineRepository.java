package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ShipmentLineEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShipmentLineRepository extends CrudRepository<ShipmentLineEntity, Integer> {

    List<ShipmentLineEntity> findByShipmentIdOrderByLineIdAsc(int shipmentId);
}
