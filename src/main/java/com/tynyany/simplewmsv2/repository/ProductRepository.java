package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<ProductEntity, Integer> {
    Optional<ProductEntity> findOneByProductCode(String productCode);

    //Page<ProductEntity> findByProductNameIsLikeIgnoreCase(String filter, Pageable pageable);
    //Page<ProductEntity> findByProductNameIsLikeIgnoreCaseOrProductCodeIsLikeIgnoreCase(String filter, Pageable pageable);
}
