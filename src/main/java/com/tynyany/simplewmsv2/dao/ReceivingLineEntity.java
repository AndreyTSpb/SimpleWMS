package com.tynyany.simplewmsv2.dao;

import com.tynyany.simplewmsv2.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="receiving_line")
public class ReceivingLineEntity{
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int receivingLineID;
    int quantityProduct; //Количество товара по документам
    int quantityReceived; //Количество принятого товара
    Timestamp expirationDate; //Срок годности (если требуется)
    int receivingID;
    int locationID;
    int productID;
    Boolean complete;
    Boolean del;
    String note; //Примечание к приемке
}
