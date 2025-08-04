package com.tynyany.simplewmsv2.entity;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class ReceivingLine {
    int receivingLineID;
    int quantityProduct; //Количество товара по документам
    int quantityReceived; //Количество принятого товара
    Timestamp expirationDate; //Срок годности (если требуется)
    int receivingID;
    int locationID;
    int productID;
    Boolean complite;
    Boolean del;
    String note; //Примечание к приемке

    public ReceivingLine(int receivingLineID, int quantityProduct, int quantityReceived, Timestamp expirationDate, int receivingID, int locationID, int productID, Boolean complite, Boolean del, String note){

        this.receivingLineID = receivingLineID;
        this.quantityProduct = quantityProduct;
        this.quantityReceived = quantityReceived;
        this.expirationDate = expirationDate;
        this.receivingID = receivingID;
        this.locationID = locationID;
        this.productID = productID;
        this.complite = complite;
        this.del = del;
        this.note = note;
    }
}
