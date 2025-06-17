package com.tynyany.simplewmsv2.service;


import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.entity.Receiving;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReceivingService {
    Receiving getByID(int receivingID);

    List<Receiving> getAll();

    void add(Receiving receiving);

    void update(Receiving receiving);

    void del(Receiving receiving);

    Page<ReceivingEntity> getAllPageable(Pageable pageable);
    Page<ReceivingEntity> getAllPageableWithFilter(Pageable pageable, String filter);
}
