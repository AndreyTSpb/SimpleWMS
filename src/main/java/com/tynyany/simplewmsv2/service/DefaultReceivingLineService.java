package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineRepository;
import com.tynyany.simplewmsv2.entity.Product;
import com.tynyany.simplewmsv2.entity.ReceivingLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultReceivingLineService implements ReceivingLineService{

    private final ReceivingLineRepository receivingLineRepository;

    @Override
    public ReceivingLine getByID(int receivingLineID) {
        return null;
    }

    @Override
    public List<ReceivingLine> getAll() {
        return null;
    }

    @Override
    public void add(ReceivingLine receivingLine) {
         ReceivingLineEntity receivingLineEntity = new ReceivingLineEntity(
                    receivingLine.getReceivingLineID(),
                    receivingLine.getQuantityProduct(),
                    receivingLine.getQuantityReceived(),
                    receivingLine.getExpirationDate(),
                    receivingLine.getReceivingID(),
                    receivingLine.getLocationID(),
                    receivingLine.getProductID(),
                    receivingLine.getDel(),
                    receivingLine.getNote()
            );
         receivingLineRepository.save(receivingLineEntity);
    }

    @Override
    public void update(ReceivingLine receivingLine) {

    }

    @Override
    public void del(ReceivingLine receivingLine) {

    }
}
