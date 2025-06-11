package com.tynyany.simplewmsv2.models;

public class CreateCodeLocation {
    private String locationCode;
    private final String row;
    private final String x; // координаты в горизонтальной плоскости
    private final String y; // координаты в вертикальной плоскости
    private final String z;
    private final String zoneCode;


    public CreateCodeLocation(String row, int x, int y, int z, String zoneCode) {
        this.row = (row.length() > 1) ? row : "0" + row;
        this.x = (x > 9) ? String.valueOf(x): "0" + x;
        this.y = String.valueOf(y);
        this.z = String.valueOf(z);
        this.zoneCode = zoneCode;
        create();
    }


    private void create(){
        this.locationCode = this.zoneCode+ "-" +this.row + "-" + this.x + "-" + this.y + "-" + this.z;
    }

    public String getLocationCode() {
        return this.locationCode;
    }
}
