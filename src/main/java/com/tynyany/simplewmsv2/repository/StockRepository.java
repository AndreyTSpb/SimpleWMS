package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockEntity;
import com.tynyany.simplewmsv2.dto.UnplacedStockRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends CrudRepository<StockEntity, Integer> {
    StockEntity findByBatchIdAndLocationId(int batchId, int locationid);
    StockEntity findByBatchIdAndLocationIdAndDel(int batchId, int locationid, Boolean del);

    Iterable<StockEntity> findByLocationIdAndDel(int locationId, Boolean del);

    /**
     * Принятые, но ещё не включённые в маршрут размещения остатки в ячейке приёмки,
     * у которых целевая ячейка (receiving_line.locationid) находится в зоне zoneId.
     */
    @Query(value = "SELECT s.stock_id AS \"stockId\", s.batch_id AS \"batchId\", s.product_id AS \"productId\"," +
            " s.quantity AS \"quantity\", rl.locationid AS \"targetLocationId\", rl.receiving_lineid AS \"receivingLineId\"" +
            " FROM stocks AS s" +
            " JOIN batches AS b ON s.batch_id = b.batch_id" +
            " JOIN receiving_line AS rl ON b.receiving_id = rl.receiving_lineid" +
            " JOIN location AS l ON rl.locationid = l.locationid" +
            " WHERE COALESCE(b.del, FALSE) = FALSE" +
            "   AND COALESCE(s.del, FALSE) = FALSE" +
            "   AND s.location_id = :receivingLocationId" +
            "   AND s.quantity > 0" +
            "   AND COALESCE(rl.del, FALSE) = FALSE" +
            "   AND COALESCE(rl.placement_route, FALSE) = FALSE" +
            "   AND rl.complete = TRUE" +
            "   AND l.zoneid = :zoneId" +
            " ORDER BY l.location_code", nativeQuery = true)
    List<UnplacedStockRow> findUnplacedInZone(@Param("zoneId") int zoneId,
                                              @Param("receivingLocationId") int receivingLocationId);
}
