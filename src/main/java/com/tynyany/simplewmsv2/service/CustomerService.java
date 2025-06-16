package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    Customer getByID(int customerId);
    List<Customer> getAll();
    void add(Customer customer);
    void update(Customer customer);
    void del(Customer customer);
    Page<CustomerEntity> getCustomersPageable(Pageable pageable);
    Page<CustomerEntity> getCustomersPageWithFilter(Pageable pageable, String filter);
}
