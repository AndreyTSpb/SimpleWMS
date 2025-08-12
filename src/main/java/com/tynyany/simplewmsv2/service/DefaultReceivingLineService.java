package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.entity.ReceivingLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultReceivingLineService implements ReceivingLineService{

    private final ReceivingLineRepository receivingLineRepository;

    @Override
    public ReceivingLine getByID(int receivingLineID) {
        ReceivingLineEntity receivingLineEntity = receivingLineRepository.findById(receivingLineID).orElse(null);

        if(receivingLineEntity == null) return null;
        return new ReceivingLine(
                receivingLineEntity.getReceivingLineID(),
                receivingLineEntity.getQuantityProduct(),
                receivingLineEntity.getQuantityReceived(),
                receivingLineEntity.getExpirationDate(),
                receivingLineEntity.getReceivingID(),
                receivingLineEntity.getLocationID(),
                receivingLineEntity.getProductID(),
                receivingLineEntity.getComplete(),
                receivingLineEntity.getPlacementRoute(),
                receivingLineEntity.getDel(),
                receivingLineEntity.getNote()
        );
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
                    receivingLine.getComplite(),
                    receivingLine.getPlacementRoute(),
                    receivingLine.getDel(),
                    receivingLine.getNote()
            );
         receivingLineRepository.save(receivingLineEntity);
    }

    @Override
    public void update(ReceivingLine receivingLine) {
        ReceivingLineEntity receivingLineEntity = new ReceivingLineEntity(
                receivingLine.getReceivingLineID(),
                receivingLine.getQuantityProduct(),
                receivingLine.getQuantityReceived(),
                receivingLine.getExpirationDate(),
                receivingLine.getReceivingID(),
                receivingLine.getLocationID(),
                receivingLine.getProductID(),
                receivingLine.getComplite(),
                receivingLine.getPlacementRoute(),
                receivingLine.getDel(),
                receivingLine.getNote()
        );
        receivingLineRepository.save(receivingLineEntity);
    }

    @Override
    public void del(ReceivingLine receivingLine) {

    }

    @Override
    public List<ReceivingLine> getAllLinesForReceiving(int receivingID) {
        ArrayList<ReceivingLineEntity> receivingLineOption = receivingLineRepository.findAllByReceivingIDAndDel(receivingID, false);
        if(receivingLineOption.isEmpty()) {
            return null;
        }
        List<ReceivingLine> resultList = new ArrayList<>();

        for(ReceivingLineEntity receivingLineEntity : receivingLineOption) {
            ReceivingLine receivingLine = convertToReceivingLine(receivingLineEntity);
            resultList.add(receivingLine);
        }
        return resultList;
    }

    @Override
    public void routeСreated(int r_l_id) {
        Optional<ReceivingLineEntity> entity = receivingLineRepository.findById(r_l_id);
        entity.ifPresent(receivingLineEntity -> receivingLineRepository.save(
                new ReceivingLineEntity(
                        receivingLineEntity.getReceivingLineID(),
                        receivingLineEntity.getQuantityProduct(),
                        receivingLineEntity.getQuantityReceived(),
                        receivingLineEntity.getExpirationDate(),
                        receivingLineEntity.getReceivingID(),
                        receivingLineEntity.getLocationID(),
                        receivingLineEntity.getProductID(),
                        receivingLineEntity.getComplete(),
                        true,
                        receivingLineEntity.getDel(),
                        receivingLineEntity.getNote()
                )
        ));

    }

    private ReceivingLine convertToReceivingLine(ReceivingLineEntity entity) {
        return new ReceivingLine(entity.getReceivingLineID(),entity.getQuantityProduct(), entity.getQuantityReceived(), entity.getExpirationDate(), entity.getReceivingID(), entity.getLocationID(), entity.getProductID(), entity.getComplete(), entity.getPlacementRoute(), entity.getDel(), entity.getNote());
    }
}
