package com.tynyany.simplewmsv2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductTableString extends Product{
    String supplierName;
    String unitOfMeasure;
    String categoryName; //категория товара
    String abcCode;
}
