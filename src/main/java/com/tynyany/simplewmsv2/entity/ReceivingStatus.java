package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class ReceivingStatus {
    int receivingStatusID;
    String receivingStatus; //"В ожидании", "В процессе", "Завершена", "Отменена"

    public ReceivingStatus(int receivingStatusID, String receivingStatus){

        this.receivingStatusID = receivingStatusID;
        this.receivingStatus = receivingStatus;
    }
}
