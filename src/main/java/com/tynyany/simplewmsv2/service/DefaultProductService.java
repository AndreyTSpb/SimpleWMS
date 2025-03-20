package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.EmployeeEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ProductRepository;
import com.tynyany.simplewmsv2.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService{

    private final ProductRepository productRepository;

    @Override
    public Product getProductByID(int productID) {
        return null;
    }

    @Override
    public List<Product> getAllProduct() {

        Iterable<ProductEntity> iterable = productRepository.findAll();

        ArrayList<Product> products = new ArrayList<>();

        for(ProductEntity productEntity : iterable){
            products.add(new Product(
                    productEntity.getProductID(),
                    productEntity.getProductName(),
                    productEntity.getProductCode(),
                    productEntity.getDescription(),
                    productEntity.getWeight(),
                    productEntity.getVolume(),
                    productEntity.getCategoryID(),
                    productEntity.getAbcID(),
                    productEntity.getMinimumStockLevel(),
                    productEntity.getExpirationDateRequired(),
                    productEntity.getDel(),
                    productEntity.getSupplierID(),
                    productEntity.getUnitOfMeasureID(),
                    productEntity.getExtBarcode(),
                    productEntity.getIntBarcode()
            ));
        }
        return products;
    }

    @Override
    public void addProduct(Product product) {
        ProductEntity productEntity = new ProductEntity(
                0,
                product.getProductName(),
                product.getProductCode(),
                product.getDescription(),
                product.getWeight(),
                product.getVolume(),
                product.getCategoryID(),
                product.getAbcID(),
                product.getMinimumStockLevel(),
                product.getExpirationDateRequired(),
                false,
                product.getSupplierID(),
                product.getUnitOfMeasureID(),
                product.getExtBarcode(),
                product.getIntBarcode()
        );
        productRepository.save(productEntity);
    }

    @Override
    public void updateProduct(Product product) {

    }

    @Override
    public void delProduct(Product product) {

    }
}
