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

}
