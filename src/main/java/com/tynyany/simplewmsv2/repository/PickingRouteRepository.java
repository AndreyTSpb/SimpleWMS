package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.PickingRouteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PickingRouteRepository extends CrudRepository<PickingRouteEntity, Integer> {

    @Query("select r from PickingRouteEntity r where r.del = false or r.del is null order by r.routeId desc")
    List<PickingRouteEntity> findActiveOrderByIdDesc();

    /** Маршруты заказа в указанных статусах (для проверки «уже передан в сборку»). */
    @Query("select r from PickingRouteEntity r where r.orderId = :orderId and r.statusId in :statuses"
            + " and (r.del = false or r.del is null) order by r.routeId desc")
    List<PickingRouteEntity> findByOrderAndStatuses(@Param("orderId") int orderId,
                                                    @Param("statuses") Collection<Integer> statuses);
}
