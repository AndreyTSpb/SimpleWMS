package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Product {
    int productID;
    String productName;
    String productCode;
    String description;
    Float weight; //вес
    Float volume; //обьем
    int categoryID; //категория товара
    int abcID; //коды А-B-C-D
    int minimumStockLevel; //Минимальный уровень запасов (для уведомлений)
    Boolean expirationDateRequired; //Требуется ли отслеживание срока годности
    //Timestamp expirationDate; //срок годности (годен до)
    Boolean del;
    int supplierID;
    int unitOfMeasureID;
    String extBarcode; //внешний бар код товара
    String intBarcode; //внутренний бар код товара

    public Product(int productID, String productName, String productCode, String description, Float weight, Float volume, int categoryID, int abcID, int minimumStockLevel, Boolean expirationDateRequired, Boolean del, int supplierID, int unitOfMeasureID, String extBarcode, String intBarcode){
        this.productID = productID;
        this.productName = productName;
        this.productCode = productCode;
        this.description = description;
        this.weight = weight;
        this.volume = volume;
        this.categoryID = categoryID;
        this.abcID = abcID;
        this.minimumStockLevel = minimumStockLevel;
        this.expirationDateRequired = expirationDateRequired;
        this.del = del;
        this.supplierID = supplierID;
        this.unitOfMeasureID = unitOfMeasureID;
        this.extBarcode = extBarcode;
        this.intBarcode = intBarcode;
    }
}
