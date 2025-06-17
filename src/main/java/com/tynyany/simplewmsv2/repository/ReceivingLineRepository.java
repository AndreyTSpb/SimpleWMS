package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public interface ReceivingLineRepository extends CrudRepository<ReceivingLineEntity, Integer> {
    ArrayList<ReceivingLineEntity> findAllByReceivingIDAndDel(Integer receiverId, Boolean del);
}
