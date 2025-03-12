package com.tynyany.simplewmsv2.entity;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class Receiving {
    int receivingID;
    Timestamp receivingDate;
    Timestamp getReceivingDate;
    String documentNumber;
    int receivingStatusID; //"В ожидании", "В процессе", "Завершена", "Отменена"
    int employeeID;
    Boolean del;

    public Receiving(int receivingID, Timestamp receivingDate, Timestamp getReceivingDate, String documentNumber, int receivingStatusID, int employeeID, Boolean del){
        this.receivingID = receivingID;
        this.receivingDate = receivingDate;
        this.getReceivingDate = getReceivingDate;
        this.documentNumber = documentNumber;

        this.receivingStatusID = receivingStatusID;
        this.employeeID = employeeID;
        this.del = del;
    }

}
