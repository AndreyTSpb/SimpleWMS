package com.tynyany.simplewmsv2.entity;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class Receiving {
    int receivingID;
    Timestamp receivingDate;
    Timestamp getReceivingDate;
    String documentNumber;
    int statusID; //"В ожидании", "В процессе", "Завершена", "Отменена"
    int employeeID;
    int supplierID;
    Boolean del;

    public Receiving(int receivingID, Timestamp receivingDate, Timestamp getReceivingDate, String documentNumber, int statusID, int employeeID, int supplierID, Boolean del){
        this.receivingID = receivingID;
        this.receivingDate = receivingDate;
        this.getReceivingDate = getReceivingDate;
        this.documentNumber = documentNumber;

        this.statusID = statusID;
        this.employeeID = employeeID;
        this.supplierID = supplierID;
        this.del = del;
    }

}
