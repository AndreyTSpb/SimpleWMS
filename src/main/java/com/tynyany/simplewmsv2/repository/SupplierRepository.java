package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.entity.Supplier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends CrudRepository<SupplierEntity, Integer> {
    Optional<SupplierEntity> findTopBySupplierCode(String supplierCode);

    Page<SupplierEntity> findBySupplierCodeIgnoreCaseOrSupplierNameIgnoreCase(String supplierCode, String supplierName, Pageable pageable);
    Page<SupplierEntity> findAll(Pageable pageable);
    Page<SupplierEntity> findBySupplierNameLikeIgnoreCaseOrSupplierCodeLikeIgnoreCase(String supplierName, String supplierCode, Pageable pageable);
}
