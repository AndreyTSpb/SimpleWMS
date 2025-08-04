package com.tynyany.simplewmsv2.entity;

import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Value
@Builder
public class ReceivingLineForm {
    int numLine; //номер строки (никуда не сохраняем)
    int productId;
    String extBarcode;
    String intBarcode;
    int qntOrder;
    int qntFact;
    @Builder.Default
    String expirationDate = "2054-01-01";
    String note;
    int zoneId;
    int locationId;
    int orderId;
    int supplierId;
    int employerId;
}
