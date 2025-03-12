package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Zone {
    int zoneID;
    String zoneName;
    String code;
    String description;
    Boolean del;

    public Zone(int zoneID, String zoneName, String code, String description, Boolean del){
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        this.code = code;
        this.description = description;
        this.del = del;
    }
}
