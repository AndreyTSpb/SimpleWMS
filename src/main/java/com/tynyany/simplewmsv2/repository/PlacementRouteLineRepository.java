package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlacementRouteLineRepository extends CrudRepository<PlacementRouteLineEntity, Integer> {

    @Query("select l from PlacementRouteLineEntity l where l.pRouteId = :routeId"
            + " and (l.del = false or l.del is null) order by l.pRouteLineId")
    List<PlacementRouteLineEntity> findByRouteId(@Param("routeId") int routeId);
}
