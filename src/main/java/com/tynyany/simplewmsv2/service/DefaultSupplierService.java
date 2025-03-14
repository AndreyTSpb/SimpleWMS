package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.dao.SupplierRepository;
import com.tynyany.simplewmsv2.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultSupplierService implements SupplierService{

    final SupplierRepository supplierRepository;

    @Override
    public Supplier getByID(int supplierID) {
        return null;
    }

    @Override
    public List<Supplier> getAllRole() {
        return null;
    }

    @Override
    public void add(Supplier supplier) {
        SupplierEntity supplierEntity = new SupplierEntity(
                0,
                supplier.getSupplierName(),
                supplier.getSupplierCode(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                false
        );
        supplierRepository.save(supplierEntity);
    }

    @Override
    public void update(Supplier supplier) {

    }

    @Override
    public void del(Supplier supplier) {

    }
}
