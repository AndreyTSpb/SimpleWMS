package com.tynyany.simplewmsv2.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data

public class PlacementRoute {
    private final int pRouteId;
    private final Timestamp dtCreate;
    private final Timestamp dtStart;
    private final Timestamp dtStop;
    int status;
    private final int employerId;
    Boolean del;

    public PlacementRoute(int pRouteId, Timestamp dtCreate, Timestamp dtStart, Timestamp dtStop, int status, int employerId, Boolean del){
        this.pRouteId = pRouteId;
        this.dtCreate = dtCreate;
        this.dtStart = dtStart;
        this.dtStop = dtStop;
        this.status = status;
        this.employerId = employerId;
        this.del = del;
    }
}
