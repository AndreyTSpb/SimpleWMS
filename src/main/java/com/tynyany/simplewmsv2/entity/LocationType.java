package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class LocationType {
    int locationTypeID;
    String locationTypeName;
    Boolean del;

    public LocationType(int locationTypeID, String locationTypeName, Boolean del){

        this.locationTypeID = locationTypeID;
        this.locationTypeName = locationTypeName;
        this.del = del;
    }
}
