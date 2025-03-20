package com.tynyany.simplewmsv2.dao;

import com.tynyany.simplewmsv2.entity.Supplier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends CrudRepository<SupplierEntity, Integer> {
    Optional<SupplierEntity> findTopBySupplierCode(String supplierCode);
}
