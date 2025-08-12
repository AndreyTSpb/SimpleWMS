package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockEntity;
import com.tynyany.simplewmsv2.dto.ResultStockDTOSelectBatchProductLocation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface StockRepository extends CrudRepository<StockEntity, Integer> {
    StockEntity findByBatchIdAndLocationId(int batchId, int locationid);
    StockEntity findByBatchIdAndLocationIdAndDel(int batchId, int locationid, Boolean del);

    Iterable<StockEntity> findByLocationIdAndDel(int locationId, Boolean del);

    @Query(value = "SELECT" +
            " s.stock_id AS id, s.batch_id AS batch_id, s.product_id AS product_id, s.quantity AS qnt, p.product_name AS product_name, p.int_bar_code AS int_barcode, p.ext_bar_code AS ext_barcode, rl.locationid AS loc_id," +
            " l.location_code AS loc_code, l.x AS x, l.y AS y, l.z AS z, (p.weight*s.quantity) AS sum_weight, (p.volume*s.quantity) AS sum_volume, rl.receiving_lineid AS r_l_id " +
            "FROM stocks AS s LEFT JOIN batches AS b ON s.batch_id = b.batch_id" +
            "        LEFT JOIN product AS p ON s.product_id = p.productid" +
            "        LEFT JOIN receiving_line AS rl ON b.receiving_id = rl.receiving_lineid" +
            "        LEFT JOIN location AS l ON rl.locationid = l.locationid " +
            "WHERE b.del = FALSE" +
            "  AND s.del = FALSE" +
            "  AND s.location_id = 1" +
            "  AND s.quantity > 0" +
            "  AND rl.del = FALSE" +
            "  AND rl.placement_route NOT IN (TRUE)" +
            "  AND rl.complete = TRUE" +
            "  AND l.zoneid IN (?1);" , nativeQuery = true)
    Iterable<ResultStockDTOSelectBatchProductLocation> findAllByLeftJoinProductBatchLocationGroupByZoneId(int ZoneId, String batchesId);
}
