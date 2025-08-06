package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Stock;
import com.tynyany.simplewmsv2.entity.StockGettingRowsUnplacedItems;

import java.util.List;

public interface StockService {
    Stock getById(int stockId);
    Stock getByBatchIdAndFromLocationId(int batchId, int fromLocationId, Boolean del);
    List<Stock> getAllInPlaceINT(); //получить все что лежит на приемке не обработанное
    List<StockGettingRowsUnplacedItems> gettingRowsUnplacedItems(int zoneId); //олучаем список неразмешенных товаров по зонам хранеия, которые лежат на приемке
    List<Integer> getListZoneProductsForUnplacedItems(); //получить список зон хранения товара который в ожидании расмещения на складе лежит на приемке
    List<Stock> getAll();
    void add(Stock stock);
    void update(Stock stock);
    void delete(int stockId);
}
