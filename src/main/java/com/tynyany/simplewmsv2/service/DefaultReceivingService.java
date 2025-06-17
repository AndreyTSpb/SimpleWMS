package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.entity.Receiving;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultReceivingService implements ReceivingService{

    private final ReceivingRepository receivingRepository;


    @Override
    public Receiving getByID(int receivingID) {
        return null;
    }

    @Override
    public List<Receiving> getAll() {
        return null;
    }

    @Override
    public void add(Receiving receiving) {
        ReceivingEntity receivingEntity = new ReceivingEntity(
                receiving.getReceivingID(),
                receiving.getReceivingDate(),
                receiving.getGetReceivingDate(),
                receiving.getDocumentNumber(),
                receiving.getStatusID(),
                receiving.getEmployeeID(),
                receiving.getSupplierID(),
                receiving.getDel()
        );
        receivingRepository.save(receivingEntity);
    }

    @Override
    public void update(Receiving receiving) {

    }

    @Override
    public void del(Receiving receiving) {

    }

    @Override
    public Page<ReceivingEntity> getAllPageable(Pageable pageable) {
        return receivingRepository.findAll(pageable);
    }

    @Override
    public Page<ReceivingEntity> getAllPageableWithFilter(Pageable pageable, String filter) {
        return receivingRepository.findByDocumentNumberLikeIgnoreCaseOrReceivingDateLikeIgnoreCase(filter, Timestamp.valueOf(filter), pageable);
    }
}
