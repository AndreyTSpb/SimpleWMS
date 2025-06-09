package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.Location;
import lombok.Value;

@Value
public class LocationTableString extends Location {
    String zoneName;

    public LocationTableString(int locationID, String locationCode, String row, int x, int y, int z, float capacity, Boolean available, Boolean del, int zoneID, String zoneName) {
        this.zoneName = zoneName;
        this.setLocationID(locationID);
        this.setLocationCode(locationCode);
        this.setRow(row);
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setCapacity(capacity);
        this.setAvailable(available);
        this.setDel(del);
        this.setZoneID(zoneID);
    }
}
