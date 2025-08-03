package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface ReceivingLineRepository extends CrudRepository<ReceivingLineEntity, Integer> {
    ArrayList<ReceivingLineEntity> findAllByReceivingIDAndDel(Integer receiverId, Boolean del);
}
