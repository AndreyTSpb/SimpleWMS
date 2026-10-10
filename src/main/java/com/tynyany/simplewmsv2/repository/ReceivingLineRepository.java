package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface ReceivingLineRepository extends CrudRepository<ReceivingLineEntity, Integer> {
    ArrayList<ReceivingLineEntity> findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(Integer receiverId, Boolean del);
}
