package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Receiving;
import com.tynyany.simplewmsv2.entity.ReceivingLine;

import java.util.List;

public interface ReceivingLineService {
    ReceivingLine getByID(int receivingLineID);

    List<ReceivingLine> getAll();

    void add(ReceivingLine receivingLine);

    void update(ReceivingLine receivingLine);

    void del(ReceivingLine receivingLine);
}
