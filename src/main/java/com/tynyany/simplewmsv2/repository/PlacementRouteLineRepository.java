package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.dto.LocationVolume;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlacementRouteLineRepository extends CrudRepository<PlacementRouteLineEntity, Integer> {

    @Query("select l from PlacementRouteLineEntity l where l.pRouteId = :routeId"
            + " and (l.del = false or l.del is null) order by l.pRouteLineId")
    List<PlacementRouteLineEntity> findByRouteId(@Param("routeId") int routeId);

    /**
     * Объём (литры), запланированный к размещению в ячейки открытыми маршрутами
     * (статусы «Создан» и «В работе», строки ещё не подтверждены).
     */
    @Query(value = "SELECT l.location_id AS \"locationId\", SUM(l.qnt * COALESCE(p.volume, 0)) AS \"volume\"" +
            " FROM placement_route_line AS l" +
            " JOIN placement_route AS r ON r.p_route_id = l.p_route_id" +
            " JOIN product AS p ON p.productid = l.product_id" +
            " WHERE COALESCE(l.complete, FALSE) = FALSE AND COALESCE(l.del, FALSE) = FALSE" +
            "   AND COALESCE(r.del, FALSE) = FALSE AND r.status IN (0, 1)" +
            " GROUP BY l.location_id", nativeQuery = true)
    List<LocationVolume> sumPlannedVolumeByLocation();
}
