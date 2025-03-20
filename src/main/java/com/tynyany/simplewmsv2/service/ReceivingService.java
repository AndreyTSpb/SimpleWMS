package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Product;
import com.tynyany.simplewmsv2.entity.Receiving;

import java.util.List;

public interface ReceivingService {
    Receiving getByID(int receivingID);

    List<Receiving> getAll();

    void add(Receiving receiving);

    void update(Receiving receiving);

    void del(Receiving receiving);
}
