package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ReceivingRepository extends CrudRepository<ReceivingEntity, Integer> {
    @Query(value = "SELECT nextval('receivings_receivingid_seq')", nativeQuery = true)
    int getNextId();

    ReceivingEntity findFirstByOrderByReceivingIDDesc();
}
