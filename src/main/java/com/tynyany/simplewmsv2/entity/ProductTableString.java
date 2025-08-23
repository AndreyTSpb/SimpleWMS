package com.tynyany.simplewmsv2.entity;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Value
public class ProductTableString extends Product{
    String categoryName;
    String abcName;
    String supplierName;

    public ProductTableString(Product product, String categoryName, String abcName, String supplierName) {

        this.categoryName = categoryName;
        this.abcName = abcName;
        this.supplierName = supplierName;
        this.productID = product.getProductID();
        this.productName = product.getProductName();
        this.productCode = product.getProductCode();
        this.description = product.getDescription();
        this.weight = product.getWeight();
        this.volume = product.getVolume();
        this.categoryID = product.getCategoryID();
        this.abcID = product.getAbcID();
        this.expirationDateRequired = product.getExpirationDateRequired();
        this.del = product.getDel();
        this.supplierID = product.getSupplierID();
        this.extBarcode = product.getExtBarcode();
        this.intBarcode = product.getIntBarcode();
    }
}
