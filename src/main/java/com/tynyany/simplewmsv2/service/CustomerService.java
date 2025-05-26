package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Customer;

import java.util.List;

public interface CustomerService {
    Customer getByID(int customerId);
    List<Customer> getAll();
    void add(Customer customer);
    void update(Customer customer);
    void del(Customer customer);
}
