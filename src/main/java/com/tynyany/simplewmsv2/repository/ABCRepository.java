package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ABCEntity;
import com.tynyany.simplewmsv2.entity.ABC;
import org.springframework.data.repository.CrudRepository;

public interface ABCRepository extends CrudRepository<ABCEntity, Integer> {
    ABC findByCode(String code);
}
