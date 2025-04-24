package com.tynyany.simplewmsv2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Data
@NoArgsConstructor
public class Product {
    int productID;
    String productName;
    String productCode;
    String description;
    Float weight; //вес
    Float volume; //обьем
    int categoryID; //категория товара
    int abcID; //коды А-B-C-D
    Boolean expirationDateRequired; //Требуется ли отслеживание срока годности
    Boolean del;
    int supplierID;
    String extBarcode; //внешний бар код товара
    String intBarcode; //внутренний бар код товара

    public Product(int productID, String productName, String productCode, String description, Float weight, Float volume, int categoryID, int abcID, Boolean expirationDateRequired, Boolean del, int supplierID, String extBarcode, String intBarcode){
        this.productID = productID;
        this.productName = productName;
        this.productCode = productCode;
        this.description = description;
        this.weight = weight;
        this.volume = volume;
        this.categoryID = categoryID;
        this.abcID = abcID;
        this.expirationDateRequired = expirationDateRequired;
        this.del = del;
        this.supplierID = supplierID;
        this.extBarcode = extBarcode;
        this.intBarcode = intBarcode;
    }

}
