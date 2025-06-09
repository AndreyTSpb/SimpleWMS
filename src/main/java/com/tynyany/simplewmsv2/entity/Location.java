package com.tynyany.simplewmsv2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

@Data
@NoArgsConstructor
public class Location {
    int locationID;
    String locationCode;
    String row;
    int x; // координаты в горизонтальной плоскости
    int y; // координаты в вертикальной плоскости
    int z; // порядковый номер полки при разделении места
    float capacity;
    Boolean available;
    Boolean del;
    int zoneID;

    public Location(int locationID, String locationCode, String row, int x, int y, int z, float capacity, Boolean available, Boolean del, int zoneID){
        this.locationID = locationID;
        this.locationCode = locationCode;
        this.row = row;
        this.x = x;
        this.y = y;
        this.z = z;

        this.capacity = capacity; //размер места
        this.available = available; //свободно или нет
        this.del = del;

        this.zoneID = zoneID;
    }
}
