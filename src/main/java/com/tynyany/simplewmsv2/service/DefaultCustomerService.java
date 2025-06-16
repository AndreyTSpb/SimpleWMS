package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        Iterable<CustomerEntity> customerEntities = customerRepository.findAll();
        ArrayList<Customer> customers = new ArrayList<>();
        for (CustomerEntity customerEntity : customerEntities) {
            customers.add(new Customer(
               customerEntity.getCustomerId(),
               customerEntity.getCustomerCode(),
               customerEntity.getCustomerName(),
               customerEntity.getPhone(),
                    customerEntity.getEmail(),
                    customerEntity.getAddress(),
                    customerEntity.getDel(),
                    customerEntity.getWorkingTimeStr(),
                    customerEntity.getWorkingTimeEnd()
            ));
        }
        return customers;
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
        CustomerEntity customerEntity = new CustomerEntity(
                customer.getCustomerID(),
                customer.getCustomerCode(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getWorkingTimeStr(),
                customer.getWorkingTimeEnd(),
                customer.getDel()
        );
        customerRepository.save(customerEntity);
    }

    @Override
    public void del(Customer customer) {
        CustomerEntity customerEntity = new CustomerEntity(
                customer.getCustomerID(),
                customer.getCustomerCode(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getWorkingTimeStr(),
                customer.getWorkingTimeEnd(),
                true
        );
        customerRepository.save(customerEntity);
    }

    @Override
    public Page<CustomerEntity> getCustomersPageable(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Page<CustomerEntity> getCustomersPageWithFilter(Pageable pageable, String filter) {
        return customerRepository.findByCustomerNameLikeIgnoreCaseOrCustomerCodeLikeIgnoreCase(filter, filter, pageable);
    }
}
