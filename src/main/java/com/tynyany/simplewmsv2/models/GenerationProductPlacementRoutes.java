package com.tynyany.simplewmsv2.models;


import com.tynyany.simplewmsv2.entity.StockGettingRowsUnplacedItems;
import com.tynyany.simplewmsv2.service.PlacementRouteLineService;
import com.tynyany.simplewmsv2.service.PlacementRouteService;
import com.tynyany.simplewmsv2.service.ReceivingLineService;
import com.tynyany.simplewmsv2.service.StockService;

import java.util.List;

public class GenerationProductPlacementRoutes {
    final StockService stockService;
    final PlacementRouteService placementRouteService;
    final PlacementRouteLineService placementRouteLineService;
    final ReceivingLineService receivingLineService;

    public GenerationProductPlacementRoutes(StockService stockService, PlacementRouteService placementRouteService, PlacementRouteLineService placementRouteLineService, ReceivingLineService receivingLineService) {
        this.stockService = stockService;
        this.placementRouteService = placementRouteService;
        this.placementRouteLineService = placementRouteLineService;
        this.receivingLineService = receivingLineService;
        stockService.getAllInPlaceINT();

        List<Integer> zoneIds = stockService.getListZoneProductsForUnplacedItems();
        for (int zoneId : zoneIds) {


            //Создаем маршрут комплектации (дележка по зонам)
            CreateProductPlacementRoute createProductPlacementRoute = new CreateProductPlacementRoute(this.placementRouteService);
            int id_row = createProductPlacementRoute.add();

            List<StockGettingRowsUnplacedItems> rows =  stockService.gettingRowsUnplacedItems(zoneId);
            System.out.println(stockService.gettingRowsUnplacedItems(zoneId));

            for (StockGettingRowsUnplacedItems item : rows) {
                //[StockGettingRowsUnplacedItems(
                // batch_id=19,
                // product_id=12,
                // qnt=100,
                // product_name=Скрепки INFORMAT 28 мм класс. оцинкованные серебро карт. упак. 100 шт,
                // int_barcode=2000253470536,
                // ext_barcode=4600950660955,
                // loc_id=13,
                // loc_code=SC2-02-05-2-1,
                // x=5,
                // y=2,
                // z=1,
                // sum_weight=0.23,
                // sum_volume=0.35099998
                // ),,
                CreateProductPlacementRouteLine createProductPlacementRouteLine = new CreateProductPlacementRouteLine(
                        placementRouteLineService,item.getBatch_id(),item.getLoc_id(),id_row,item.getProduct_id(), item.getProduct_id()
                );
                //отмечаем что обработали эту строку прихода
                if(createProductPlacementRouteLine.addLine()>0){
                    receivingLineService.routeСreated(item.getR_l_id());
                }
            }


        }
    }
}
