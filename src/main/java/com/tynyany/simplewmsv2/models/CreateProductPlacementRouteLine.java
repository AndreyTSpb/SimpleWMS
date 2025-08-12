package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.PlacementRouteLine;
import com.tynyany.simplewmsv2.service.PlacementRouteLineService;
import lombok.Value;

/**
 * Добавляем строку в маршрут размещения товаров
 */
@Value
public class CreateProductPlacementRouteLine {
    PlacementRouteLineService placementRouteLineService;
    int batch_id;
    int location_id;
    int p_route_id;
    int product_id;
    int qnt;

    public CreateProductPlacementRouteLine(PlacementRouteLineService placementRouteLineService, int batch_id, int location_id, int p_route_id, int product_id, int qnt) {
        this.placementRouteLineService = placementRouteLineService;
        this.batch_id = batch_id;
        this.location_id = location_id;
        this.p_route_id = p_route_id;
        this.product_id = product_id;
        this.qnt = qnt;
    }

    public int addLine(){
        return this.placementRouteLineService.add(
                new PlacementRouteLine(
                        0,
                        this.p_route_id,
                        this.qnt,
                        this.location_id,
                        this.batch_id,
                        this.product_id,
                        false,
                        false
                )
        );
    }
}
