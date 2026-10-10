package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.PickingRouteLineEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PickingRouteLineRepository extends CrudRepository<PickingRouteLineEntity, Integer> {

    List<PickingRouteLineEntity> findByRouteIdOrderBySortOrderAscLineIdAsc(int routeId);
}
