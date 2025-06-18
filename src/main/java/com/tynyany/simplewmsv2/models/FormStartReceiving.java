package com.tynyany.simplewmsv2.models;

import lombok.Getter;

import java.util.Date;


public class FormStartReceiving {
    @Getter
    private String date_start;
    @Getter
    private Integer employeeID;
    @Getter
    private Integer orderID;

    public FormStartReceiving(String dt, String employeeID, String orderID){
        this.date_start = dt+" 00:00:00";
        this.employeeID = Integer.valueOf(employeeID);
        this.orderID = Integer.valueOf(orderID);
    }
}
