package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="product")
public class ProductEntity {

    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productID;

    private String productName;
    private String productCode;
    private String description;
    private Float weight; //вес
    private Float volume; //обьем
    private Boolean expirationDateRequired = false; //Требуется ли отслеживание срока годности
    //Timestamp expirationDate; //срок годности (годен до)
    private Boolean del = false;
    private int supplierID;
    private int categoryID; //категория товара
    private String extBarCode; //внешний бар код товара
    private String intBarCode; //внутренний бар код товара
    private int abcID; //коды А-B-C-D
}
