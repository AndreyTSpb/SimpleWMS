package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerRepository;
import com.tynyany.simplewmsv2.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultCustomerService implements CustomerService {
    final CustomerRepository customerRepository;

    @Override
    public Customer getByID(int customerId) {
        return null;
    }

    @Override
    public List<Customer> getAll() {
        return List.of();
    }

    @Override
    public void add(Customer customer) {
        CustomerEntity customerEntity = new CustomerEntity(
                0,
                customer.getCustomerCode(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getWorkingTimeStr(),
                customer.getWorkingTimeEnd(),
                false
        );
        customerRepository.save(customerEntity);
    }

    @Override
    public void update(Customer customer) {

    }

    @Override
    public void del(Customer customer) {

    }
}
