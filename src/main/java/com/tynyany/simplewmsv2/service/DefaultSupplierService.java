package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.repository.SupplierRepository;
import com.tynyany.simplewmsv2.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultSupplierService implements SupplierService{

    final SupplierRepository supplierRepository;

    @Override
    public Supplier getByID(int supplierID) {
        SupplierEntity supplierEntity = supplierRepository.findById(supplierID).orElse(null);
        if(supplierEntity == null) return null;
        return new Supplier(supplierEntity.getSupplierID(),
                supplierEntity.getSupplierName(),
                supplierEntity.getSupplierCode(),
                supplierEntity.getContactPerson(),
                supplierEntity.getPhoneNumber(),
                supplierEntity.getEmail(),
                supplierEntity.getDel());
    }

    @Override
    public List<Supplier> getAll() {
        Iterable<SupplierEntity> iterable = supplierRepository.findAll();

        ArrayList<Supplier> suppliers = new ArrayList<>();
        for (SupplierEntity supplierEntity : iterable){
            suppliers.add(new Supplier(
                    supplierEntity.getSupplierID(),
                    supplierEntity.getSupplierName(),
                    supplierEntity.getSupplierCode(),
                    supplierEntity.getContactPerson(),
                    supplierEntity.getPhoneNumber(),
                    supplierEntity.getEmail(),
                    supplierEntity.getDel()
            ));
        }
        return suppliers;
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

    @Override
    public Page<SupplierEntity> getAllSupplierWithPageable(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    @Override
    public Page<SupplierEntity> getAllSupplierWithPageableAndFiler(Pageable pageable, String filter) {
        return supplierRepository.findBySupplierNameLikeIgnoreCaseOrSupplierCodeLikeIgnoreCase(filter, filter, pageable);
    }
}
