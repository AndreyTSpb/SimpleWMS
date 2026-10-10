package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.StockReservationEntity;
import com.tynyany.simplewmsv2.dto.AvailableStockRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockReservationRepository extends CrudRepository<StockReservationEntity, Integer> {

    List<StockReservationEntity> findByOrderIdOrderByReservationIdAsc(int orderId);

    List<StockReservationEntity> findByOrderLineId(int orderLineId);

    /** Сколько единиц партии в ячейке зарезервировано под заказы. */
    @Query(value = "SELECT COALESCE(SUM(r.quantity), 0) FROM stock_reservation AS r" +
            " WHERE r.batch_id = :batchId AND r.location_id = :locationId", nativeQuery = true)
    int sumReserved(@Param("batchId") int batchId, @Param("locationId") int locationId);

    /**
     * Свободные остатки товара в ячейках хранения в порядке FEFO:
     * ближайший срок годности первым, партии без срока — последними, затем более ранний приход.
     */
    @Query(value = "SELECT s.batch_id AS \"batchId\", s.location_id AS \"locationId\"," +
            " s.quantity - COALESCE((SELECT SUM(r.quantity) FROM stock_reservation AS r" +
            "     WHERE r.batch_id = s.batch_id AND r.location_id = s.location_id), 0) AS \"available\"," +
            " b.expiry_date AS \"expiryDate\"" +
            " FROM stocks AS s" +
            " JOIN batches AS b ON b.batch_id = s.batch_id" +
            " JOIN location AS l ON l.locationid = s.location_id" +
            " WHERE s.product_id = :productId AND s.quantity > 0 AND COALESCE(s.del, FALSE) = FALSE" +
            "   AND COALESCE(b.del, FALSE) = FALSE AND l.del = 0" +
            "   AND s.location_id NOT IN (:excludedLocations)" +
            " ORDER BY b.expiry_date ASC NULLS LAST, b.date_received ASC, s.batch_id, s.location_id",
            nativeQuery = true)
    List<AvailableStockRow> findAvailableFefo(@Param("productId") int productId,
                                              @Param("excludedLocations") Collection<Integer> excludedLocations);
}
