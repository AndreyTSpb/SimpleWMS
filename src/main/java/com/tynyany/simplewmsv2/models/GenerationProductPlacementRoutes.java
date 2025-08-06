package com.tynyany.simplewmsv2.models;


import com.tynyany.simplewmsv2.service.StockService;

import java.util.List;

public class GenerationProductPlacementRoutes {
    final StockService stockService;

    public GenerationProductPlacementRoutes(StockService stockService) {
        this.stockService = stockService;
        stockService.getAllInPlaceINT();

        List<Integer> zoneIds = stockService.getListZoneProductsForUnplacedItems();
        for (int zoneId : zoneIds) {
            System.out.println(stockService.gettingRowsUnplacedItems(zoneId));
        }
    }
}
