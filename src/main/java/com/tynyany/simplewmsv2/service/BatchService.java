package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Batch;

import java.util.List;

public interface BatchService {
    Batch getById(int batche_id);
    List<Batch> getAll();
    int add(Batch batche);
    void update(Batch batche);
    void delete(int batche_id);
}
