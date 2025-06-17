package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Optional;

public interface ReceivingRepository extends CrudRepository<ReceivingEntity, Integer> {
    @Query(value = "SELECT nextval('receivings_receivingid_seq')", nativeQuery = true)
    int getNextId();

    ReceivingEntity findFirstByOrderByReceivingIDDesc();

    Page<ReceivingEntity> findAll(Pageable pageable);
    Page<ReceivingEntity> findByDocumentNumberLikeIgnoreCaseOrReceivingDateLikeIgnoreCase(String documentNumber, Timestamp receivingDate, Pageable pageable);

    Page<ReceivingEntity> findByDocumentNumberLikeIgnoreCase(String filter, Pageable pageable);
}
