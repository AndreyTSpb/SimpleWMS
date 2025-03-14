package com.tynyany.simplewmsv2.dao;

import com.tynyany.simplewmsv2.entity.ABC;
import org.springframework.data.repository.CrudRepository;

public interface ABCRepository extends CrudRepository<ABCEntity, Integer> {
    ABC findByCode(String code);
}
