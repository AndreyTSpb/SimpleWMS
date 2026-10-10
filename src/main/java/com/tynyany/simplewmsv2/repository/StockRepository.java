package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockEntity;
import com.tynyany.simplewmsv2.dto.LocationVolume;
import com.tynyany.simplewmsv2.dto.StockPositionRow;
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
     * Принятые, но ещё не включённые в маршрут размещения остатки в ячейке приёмки.
     * targetLocationId — ячейка, выбранная при приёмке строки (0 — не выбрана, подбирается автоматически).
     */
    @Query(value = "SELECT s.stock_id AS \"stockId\", s.batch_id AS \"batchId\", s.product_id AS \"productId\"," +
            " s.quantity AS \"quantity\", COALESCE(rl.locationid, 0) AS \"targetLocationId\"," +
            " rl.receiving_lineid AS \"receivingLineId\"" +
            " FROM stocks AS s" +
            " JOIN batches AS b ON s.batch_id = b.batch_id" +
            " JOIN receiving_line AS rl ON b.receiving_id = rl.receiving_lineid" +
            " WHERE COALESCE(b.del, FALSE) = FALSE" +
            "   AND COALESCE(s.del, FALSE) = FALSE" +
            "   AND s.location_id = :receivingLocationId" +
            "   AND s.quantity > 0" +
            "   AND COALESCE(rl.del, FALSE) = FALSE" +
            "   AND COALESCE(rl.placement_route, FALSE) = FALSE" +
            "   AND rl.complete = TRUE" +
            " ORDER BY s.stock_id", nativeQuery = true)
    List<UnplacedStockRow> findUnplaced(@Param("receivingLocationId") int receivingLocationId);

    /** Занятый объём ячеек (литры): остаток × объём единицы товара. */
    @Query(value = "SELECT s.location_id AS \"locationId\", SUM(s.quantity * COALESCE(p.volume, 0)) AS \"volume\"" +
            " FROM stocks AS s JOIN product AS p ON s.product_id = p.productid" +
            " WHERE s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE" +
            " GROUP BY s.location_id", nativeQuery = true)
    List<LocationVolume> sumVolumeByLocation();

    /** Ячейки, где лежит товар. */
    @Query(value = "SELECT DISTINCT s.location_id FROM stocks AS s" +
            " WHERE s.product_id = :productId AND s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE", nativeQuery = true)
    List<Integer> findLocationsWithProduct(@Param("productId") int productId);

    /** Суммарный остаток в ячейке (для признака занятости). */
    @Query(value = "SELECT COALESCE(SUM(s.quantity), 0) FROM stocks AS s" +
            " WHERE s.location_id = :locationId AND COALESCE(s.del, FALSE) = FALSE", nativeQuery = true)
    long sumQuantityInLocation(@Param("locationId") int locationId);

    /** Содержимое ячейки: партии с остатком. */
    @Query(value = "SELECT s.stock_id AS \"stockId\", s.batch_id AS \"batchId\", s.product_id AS \"productId\"," +
            " p.product_code AS \"productCode\", p.product_name AS \"productName\", b.expiry_date AS \"expiryDate\"," +
            " s.quantity AS \"quantity\", s.location_id AS \"locationId\", l.location_code AS \"locationCode\"" +
            " FROM stocks AS s" +
            " JOIN product AS p ON p.productid = s.product_id" +
            " JOIN location AS l ON l.locationid = s.location_id" +
            " LEFT JOIN batches AS b ON b.batch_id = s.batch_id" +
            " WHERE s.location_id = :locationId AND s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE" +
            " ORDER BY p.product_name, s.batch_id", nativeQuery = true)
    List<StockPositionRow> findPositionsInLocation(@Param("locationId") int locationId);

    /** Где лежит товар: поиск по коду или наименованию (подстрока) либо по штрихкоду (точно). */
    @Query(value = "SELECT s.stock_id AS \"stockId\", s.batch_id AS \"batchId\", s.product_id AS \"productId\"," +
            " p.product_code AS \"productCode\", p.product_name AS \"productName\", b.expiry_date AS \"expiryDate\"," +
            " s.quantity AS \"quantity\", s.location_id AS \"locationId\", l.location_code AS \"locationCode\"" +
            " FROM stocks AS s" +
            " JOIN product AS p ON p.productid = s.product_id" +
            " JOIN location AS l ON l.locationid = s.location_id" +
            " LEFT JOIN batches AS b ON b.batch_id = s.batch_id" +
            " WHERE s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE" +
            "   AND (p.product_code ILIKE :pattern OR p.product_name ILIKE :pattern" +
            "        OR p.ext_bar_code = :exact OR p.int_bar_code = :exact)" +
            " ORDER BY p.product_name, l.location_code, s.batch_id", nativeQuery = true)
    List<StockPositionRow> findPositionsByProduct(@Param("pattern") String pattern, @Param("exact") String exact);

    /** Занятый объём ячейки, л. */
    @Query(value = "SELECT COALESCE(SUM(s.quantity * COALESCE(p.volume, 0)), 0) FROM stocks AS s" +
            " JOIN product AS p ON s.product_id = p.productid" +
            " WHERE s.location_id = :locationId AND s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE", nativeQuery = true)
    double sumVolumeInLocation(@Param("locationId") int locationId);
}
