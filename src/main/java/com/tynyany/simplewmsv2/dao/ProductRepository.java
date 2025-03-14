package com.tynyany.simplewmsv2.dao;

import com.tynyany.simplewmsv2.entity.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<ProductEntity, Integer> {
    Optional<ProductEntity> findOneByProductCode(String productCode);
}
