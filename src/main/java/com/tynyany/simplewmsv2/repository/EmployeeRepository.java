package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.EmployeeEntity;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<EmployeeEntity, Integer> {
}
