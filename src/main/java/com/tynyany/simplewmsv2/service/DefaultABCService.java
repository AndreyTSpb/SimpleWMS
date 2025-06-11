package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ABCEntity;
import com.tynyany.simplewmsv2.repository.ABCRepository;
import com.tynyany.simplewmsv2.entity.ABC;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultABCService implements ABCService{

    final ABCRepository abcRepository;

    @Override
    public ABC getByID(int abcID) {

        return null;
    }

    @Override
    public List<ABC> getAll() {
        Iterable<ABCEntity> iterable = abcRepository.findAll();
        ArrayList<ABC> abcs = new ArrayList<>();
        for (ABCEntity abcEntity : iterable){
            abcs.add(new ABC(
                    abcEntity.getAbcID(),
                    abcEntity.getCode(),
                    abcEntity.getDescription(),
                    abcEntity.getDel()
            ));
        }
        return abcs;
    }

    @Override
    public void addABC(ABC abc) {
        ABCEntity abcEntity = new ABCEntity(
                0,
                abc.getCode(),
                abc.getDescription(),
                false
        );
        abcRepository.save(abcEntity);
    }

    @Override
    public void updateABC(ABC abc) {

    }

    @Override
    public void delABC(ABC abc) {

    }
}
