package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Product;
import com.tynyany.simplewmsv2.entity.ProductTableString;

import java.util.List;

public interface ProductService {
    Product getProductByID(int productID);
    List<Product> getAllProduct();
    List<ProductTableString> getAllProductTableString();
    void addProduct(Product product);

    void updateProduct(Product product);

    void delProduct(Product product);
}
