package com.tynyany.simplewmsv2.entity;

import lombok.Data;

@Data
public class PlacementRouteLine {
    private final int pRouteLineId;
    private final int pRouteId;
    private final int qnt;
    private final int locationId;
    private final int batchId;
    private final int productId;
    private final Boolean complete;
    private final Boolean del;

    public PlacementRouteLine(int pRouteLineId, int pRouteId, int qnt, int locationId, int batchId, int productId, Boolean complete, Boolean del){
        this.pRouteLineId = pRouteLineId;

        this.pRouteId = pRouteId;
        this.qnt = qnt;
        this.locationId = locationId;
        this.batchId = batchId;
        this.productId = productId;
        this.complete = complete;
        this.del = del;
    }
}
