package com.tynyany.simplewmsv2.entity;

import lombok.Value;

import java.sql.Time;

@Value
public class Customer {
    int customerID;
    String customerCode;
    String customerName;
    String phone;
    String email;
    String address;
    Boolean del;
    String workingTimeStr;
    String workingTimeEnd;

    public Customer(int customerID, String customerCode, String customerName, String phone, String email, String address, Boolean del, String workingTimeStr, String workingTimeEnd){
        this.customerID = customerID;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.del = del;
        this.workingTimeStr = workingTimeStr;
        this.workingTimeEnd = workingTimeEnd;
    }
}
