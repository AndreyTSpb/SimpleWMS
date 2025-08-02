package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Stock;

import java.util.List;

public interface StockService {
    Stock getById(int stockId);
    List<Stock> getAll();
    void add(Stock stock);
    void update(Stock stock);
    void delete(int stockId);
}
