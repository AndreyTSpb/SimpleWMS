package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplierService {
    Supplier getByID(int supplierID);
    List<Supplier> getAll();
    void add(Supplier supplier);

    void update(Supplier supplier);

    void del(Supplier supplier);

    Page<SupplierEntity> getAllSupplierWithPageable(Pageable pageable);
    Page<SupplierEntity> getAllSupplierWithPageableAndFiler(Pageable pageable, String filter);

}
