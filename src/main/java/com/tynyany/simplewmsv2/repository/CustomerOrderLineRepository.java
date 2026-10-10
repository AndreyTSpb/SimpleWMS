package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.CustomerOrderLineEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomerOrderLineRepository extends CrudRepository<CustomerOrderLineEntity, Integer> {

    List<CustomerOrderLineEntity> findByOrderIdOrderByLineIdAsc(int orderId);
}
