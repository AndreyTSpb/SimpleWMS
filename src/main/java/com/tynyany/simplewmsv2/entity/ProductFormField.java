package com.tynyany.simplewmsv2.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
public class ProductFormField {
    int productID;
    String productName;
    String productCode;
    String description;
    String weight; //вес
    String volume; //обьем
    int categoryID; //категория товара
    int abcID; //коды А-B-C-D
    Boolean expirationDateRequired; //Требуется ли отслеживание срока годности
    Boolean del;
    int supplierID;
    String extBarcode; //внешний бар код товара
    String intBarcode; //внутренний бар код товара

    public ProductFormField(int productID, String productName, String productCode, String description, String weight, String volume, int categoryID, int abcID, String expirationDateRequired, String del, int supplierID, String extBarcode, String intBarcode){
        this.productID = productID;
        this.productName = productName;
        this.productCode = productCode;
        this.description = description;
        this.weight = weight;
        this.volume = volume;
        this.categoryID = categoryID;
        this.abcID = abcID;
        this.expirationDateRequired = expirationDateRequired != null;
        this.del = del != null;
        this.supplierID = supplierID;
        this.extBarcode = extBarcode;
        this.intBarcode = intBarcode;
    }
}
