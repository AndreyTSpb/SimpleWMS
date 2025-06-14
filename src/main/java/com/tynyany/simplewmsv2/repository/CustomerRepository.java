package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.entity.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<CustomerEntity, Integer> {
    Optional<CustomerEntity> findByCustomerCode(String customerCode);
}
