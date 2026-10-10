package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends CrudRepository<CustomerOrderEntity, Integer> {

    List<CustomerOrderEntity> findAllByOrderByOrderIdDesc();

    /** Заказ с таким номером в любом статусе, кроме указанного (используется для «не отменён»). */
    Optional<CustomerOrderEntity> findFirstByDocumentNumberAndStatusIdNot(String documentNumber, int statusId);
}
