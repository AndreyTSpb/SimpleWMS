package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.dao.ReceivingRepository;
import com.tynyany.simplewmsv2.entity.Receiving;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

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
                receiving.getReceivingStatusID(),
                receiving.getEmployeeID(),
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
}
