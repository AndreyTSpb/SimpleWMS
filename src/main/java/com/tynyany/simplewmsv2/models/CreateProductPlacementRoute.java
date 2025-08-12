package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.PlacementRoute;
import com.tynyany.simplewmsv2.service.PlacementRouteService;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CreateProductPlacementRoute {
    final PlacementRouteService placementRouteService;
    public CreateProductPlacementRoute(PlacementRouteService placementRouteService){
        this.placementRouteService = placementRouteService;
    }
    public int add(){
        Timestamp t = Timestamp.valueOf(LocalDateTime.now());
        return placementRouteService.add(new PlacementRoute(
                0,
                Timestamp.valueOf(LocalDateTime.now()),
                null,
                null,
                0,
                0,
                false
        ));
    }
}
