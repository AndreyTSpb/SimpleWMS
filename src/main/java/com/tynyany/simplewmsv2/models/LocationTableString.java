package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.Location;
import lombok.Value;

@Value
public class LocationTableString {
    String zoneName;
    int locationID;
    String locationCode;
    String row;
    int x;
    int y;
    int z;
    float capacity;
    Boolean available;
    Boolean del;
    int zoneID;

    public LocationTableString(int locationID, String locationCode, String row, int x, int y, int z, float capacity, Boolean available, Boolean del, int zoneID, String zoneName) {
        this.zoneName = zoneName;
        this.locationID = locationID;
        this.locationCode = locationCode;
        this.row = row;
        this.x = x;
        this.y = y;
        this.z = z;
        this.capacity = capacity;
        this.available = available;
        this.del = del;
        this.zoneID = zoneID;
    }
}
