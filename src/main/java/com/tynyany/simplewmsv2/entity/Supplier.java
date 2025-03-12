package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Supplier {
    int supplierID;
    String supplierName;
    String supplierCode;
    String contactPerson;
    String phone;
    String email;
    Boolean del;

    public Supplier(int supplierID, String supplierName, String supplierCode, String contactPerson, String phone, String email, Boolean del){
        this.supplierID = supplierID;
        this.supplierName = supplierName;
        this.supplierCode = supplierCode;
        this.contactPerson = contactPerson;

        this.phone = phone;
        this.email = email;
        this.del = del;
    }
}
