package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.entity.Product;
import com.tynyany.simplewmsv2.entity.ProductTableString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product getProductByID(int productID);

    List<Product> getAllProduct();

    List<ProductTableString> getAllProductTableString();

    Page<ProductEntity> getAllProductWithPaging(Pageable pageable);

    Page<ProductEntity> getAllProductWithPagingAndFilter(Pageable pageable, String filter);

    void addProduct(Product product);

    void updateProduct(Product product);

    void delProduct(Product product);
}
