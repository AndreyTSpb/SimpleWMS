package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.StockEntity;
import com.tynyany.simplewmsv2.dto.ResultStockDTOSelectBatchProductLocation;
import com.tynyany.simplewmsv2.entity.Stock;
import com.tynyany.simplewmsv2.entity.StockGettingRowsUnplacedItems;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultStockService implements StockService {
    private final StockRepository stockRepository;
    private final ReceivingLineRepository receivingLineRepository;

    @Override
    public Stock getById(int stockId) {
        StockEntity stockEntity = stockRepository.findById(stockId).orElse(null);
        if (stockEntity == null) return null;

        return new Stock(
                stockEntity.getStockId(),
                stockEntity.getProduct_id(),
                stockEntity.getBatchId(),
                stockEntity.getLocationId(),
                stockEntity.getQuantity(),
                stockEntity.getDel()
                );
    }

    @Override
    public Stock getByBatchIdAndFromLocationId(int batchId, int fromLocationId, Boolean delete) {
        StockEntity stockEntity = stockRepository.findByBatchIdAndLocationIdAndDel(batchId, fromLocationId, delete);
        if (stockEntity == null) {
            new RuntimeException("Не найден остаток для партии " + batchId + " в местоположении " + fromLocationId);
            return null;
        };

        return new Stock(
                stockEntity.getStockId(),
                stockEntity.getProduct_id(),
                stockEntity.getBatchId(),
                stockEntity.getLocationId(),
                stockEntity.getQuantity(),
                stockEntity.getDel()
        );
    }

    @Override
    public List<Stock> getAllInPlaceINT() {
        Iterable<StockEntity> stockIterable = stockRepository.findByLocationIdAndDel(1, false);
        System.out.println(stockIterable);
        return null;
    }

    @Override
    public List<StockGettingRowsUnplacedItems> gettingRowsUnplacedItems(int zoneId) {
        //Gjkexbnm cgbcjr
        Iterable<ResultStockDTOSelectBatchProductLocation> iterable = stockRepository.findAllByLeftJoinProductBatchLocationGroupByZoneId(zoneId, "0");
        ArrayList<StockGettingRowsUnplacedItems> arr = new ArrayList<>();
        for(ResultStockDTOSelectBatchProductLocation entity : iterable){
            arr.add(
                    new StockGettingRowsUnplacedItems(
                            entity.getBatch_id(),
                            entity.getProduct_id(),
                            entity.getQnt(),
                            entity.getProduct_name(),
                            entity.getInt_barcode(),
                            entity.getExt_barcode(),
                            entity.getLoc_id(),
                            entity.getLoc_code(),
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            entity.getSum_weight(),
                            entity.getSum_volume(),
                            entity.getR_l_id()
                    )
            );
        }
        return arr;
    }

    @Override
    public List<Integer> getListZoneProductsForUnplacedItems() {
        return receivingLineRepository.findALLbyGroupZoneId();
    }


    @Override
    public List<Stock> getAll() {
        return List.of();
    }

    @Override
    public void add(Stock stock) {
        StockEntity stockEntity = new StockEntity(
                0,
                stock.getProduct_id(),
                stock.getBatchId(),
                stock.getLocationId(),
                stock.getQuantity(),
                stock.getDel()
        );
        stockRepository.save(stockEntity);
    }

    @Override
    public void update(Stock stock) {
        StockEntity stockEntity = new StockEntity(
                stock.getStockId(),
                stock.getProduct_id(),
                stock.getBatchId(),
                stock.getLocationId(),
                stock.getQuantity(),
                stock.getDel()
        );
        stockRepository.save(stockEntity);
    }

    @Override
    public void delete(int stockId) {
        Stock stock = getById(stockId);
        StockEntity stockEntity = new StockEntity(
                stock.getStockId(),
                stock.getProduct_id(),
                stock.getBatchId(),
                stock.getLocationId(),
                stock.getQuantity(),
                stock.getDel()
        );
        stockRepository.save(stockEntity);
    }
}
