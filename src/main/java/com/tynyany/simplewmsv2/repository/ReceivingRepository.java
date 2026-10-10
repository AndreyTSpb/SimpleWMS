package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.util.Optional;

public interface ReceivingRepository extends CrudRepository<ReceivingEntity, Integer> {
    @Query(value = "SELECT nextval('receivings_receivingid_seq')", nativeQuery = true)
    int getNextId();

    ReceivingEntity findFirstByOrderByReceivingIDDesc();

    /** Накладная с таким номером ERP в любом статусе, кроме указанного (используется для «не отменена»). */
    Optional<ReceivingEntity> findFirstByDocumentNumberAndStatusIDNot(String documentNumber, int statusID);

    Page<ReceivingEntity> findAll(Pageable pageable);

    Page<ReceivingEntity> findByDocumentNumberLikeIgnoreCase(String filter, Pageable pageable);


    /** Последняя накладная с таким номером (в т.ч. отменённая) — для запроса статуса из ERP. */
    Optional<ReceivingEntity> findFirstByDocumentNumberOrderByReceivingIDDesc(String documentNumber);

    /** Накладные в статусе statusId, приёмка которых начата не раньше from. */
    @Query("select r from ReceivingEntity r where r.statusID = :statusId and (r.del = false or r.del is null)"
            + " and r.getReceivingDate >= :from order by r.receivingID")
    java.util.List<ReceivingEntity> findByStatusSince(@org.springframework.data.repository.query.Param("statusId") int statusId,
                                                      @org.springframework.data.repository.query.Param("from") java.sql.Timestamp from);
}
