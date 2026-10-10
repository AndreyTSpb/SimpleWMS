package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ShipmentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends CrudRepository<ShipmentEntity, Integer> {

    @Query("select s from ShipmentEntity s where s.del = false or s.del is null order by s.shipmentId desc")
    List<ShipmentEntity> findActiveOrderByIdDesc();

    @Query("select s from ShipmentEntity s where (s.del = false or s.del is null) and s.shippedAt >= :from"
            + " order by s.shipmentId")
    List<ShipmentEntity> findShippedSince(@Param("from") Timestamp from);

    @Query("select s from ShipmentEntity s where s.orderId = :orderId and (s.del = false or s.del is null)")
    Optional<ShipmentEntity> findActiveByOrderId(@Param("orderId") int orderId);
}
