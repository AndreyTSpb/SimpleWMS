package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlacementRouteRepository extends CrudRepository<PlacementRouteEntity, Integer> {

    @Query("select r from PlacementRouteEntity r where r.del = false or r.del is null order by r.pRouteId desc")
    List<PlacementRouteEntity> findActiveOrderByIdDesc();
}
