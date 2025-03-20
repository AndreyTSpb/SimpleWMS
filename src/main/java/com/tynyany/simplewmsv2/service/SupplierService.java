package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Supplier;

import java.util.List;

public interface SupplierService {
    Supplier getByID(int supplierID);
    List<Supplier> getAll();
    void add(Supplier supplier);

    void update(Supplier supplier);

    void del(Supplier supplier);
}
