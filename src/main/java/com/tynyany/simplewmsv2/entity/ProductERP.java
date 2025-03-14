package com.tynyany.simplewmsv2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
public class ProductERP {
    String productName;
    String productCode;
    Float weight; //вес
    Float volume; //обьем
    String categoryName; //категория товара
    String abcCode; //коды А-B-C-D
    String supplierCode;
    String unitOfMeasure;
    String extBarcode; //внешний бар код товара
    String intBarcode; //внутренний бар код товара

    String upacovka; //варианты упаковки

    public ProductERP(String productName, String productCode, Float weight, Float volume, String categoryName, String abcCode, String supplierCode, String unitOfMeasure, String extBarcode, String intBarcode){
        this.productName = productName;
        this.productCode = productCode;
        this.weight = weight;
        this.volume = volume;
        this.categoryName = categoryName;
        this.abcCode = abcCode;

        this.supplierCode = supplierCode;
        this.unitOfMeasure = unitOfMeasure;
        this.extBarcode = extBarcode;
        this.intBarcode = intBarcode;
    }
}
