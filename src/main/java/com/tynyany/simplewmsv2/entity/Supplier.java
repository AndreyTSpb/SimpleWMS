package com.tynyany.simplewmsv2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
public class Supplier {
   public int supplierID;
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
