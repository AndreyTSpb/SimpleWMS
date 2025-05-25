package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonAutoDetect
public class ProductERP {
    String productName;
    String productCode;
    Float weight;
    Float volume;
    String categoryName;
    String abcCode;
    String supplierCode;
    String unitOfMeasure;
    String extBarcode;
    String intBarcode;
    String upacovka;

//    public ProductERP(String productName, String productCode, Float weight, Float volume, String categoryName, String abcCode, String supplierCode, String unitOfMeasure, String extBarcode, String intBarcode, String upacovka) {
//        this.productName = productName;
//        this.productCode = productCode;
//        this.weight = weight;
//        this.volume = volume;
//        this.categoryName = categoryName;
//        this.abcCode = abcCode;
//        this.supplierCode = supplierCode;
//        this.unitOfMeasure = unitOfMeasure;
//        this.extBarcode = extBarcode;
//        this.intBarcode = intBarcode;
//        this.upacovka = upacovka;
//    }
}
