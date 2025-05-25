package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.*;
import com.tynyany.simplewmsv2.entity.Product;
import com.tynyany.simplewmsv2.entity.ProductTableString;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ABCRepository abcRepository;
    private final SupplierRepository supplierRepository;

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
                    productEntity.getExpirationDateRequired(),
                    productEntity.getDel(),
                    productEntity.getSupplierID(),
                    productEntity.getExtBarCode(),
                    productEntity.getIntBarCode()
            ));
        }
        return products;
    }

    @Override
    public List<ProductTableString> getAllProductTableString() {
        Iterable<ProductEntity> iterable = productRepository.findAll();
        ArrayList<ProductTableString> products = new ArrayList<>();
        for (ProductEntity productEntity : iterable) {
            String categoryName = "";
            if(productEntity.getCategoryID() != 0){
                //categoryName = categoryRepository.findById(productEntity.getCategoryID()).get().getCategoryName();
                Optional<CategoryEntity> categoryEntity = categoryRepository.findById(productEntity.getCategoryID());
                if(categoryEntity.isPresent()){
                    categoryName = categoryEntity.get().getCategoryName();
                }
            }

            String adcName = "";
            if(productEntity.getAbcID() != 0){
                Optional<ABCEntity> abcEntity = abcRepository.findById(productEntity.getAbcID());
                if(abcEntity.isPresent()){
                    adcName = abcEntity.get().getCode();
                }
            }

            String supplierName = "";
            if(productEntity.getSupplierID() != 0){
                Optional<SupplierEntity> supplierEntity = supplierRepository.findById(productEntity.getSupplierID());
                if(supplierEntity.isPresent()){
                    supplierName = supplierEntity.get().getSupplierName();
                }
            }

            products.add(new ProductTableString(
                    new Product(
                            productEntity.getProductID(),
                            productEntity.getProductName(),
                            productEntity.getProductCode(),
                            productEntity.getDescription(),
                            productEntity.getWeight(),
                            productEntity.getVolume(),
                            productEntity.getCategoryID(),
                            productEntity.getAbcID(),
                            productEntity.getExpirationDateRequired(),
                            productEntity.getDel(),
                            productEntity.getSupplierID(),
                            productEntity.getExtBarCode(),
                            productEntity.getIntBarCode()
                    ),
                    categoryName,
                    adcName,
                    supplierName)
            );
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
                product.getExpirationDateRequired(),
                false,
                product.getSupplierID(),
                product.getCategoryID(),
                product.getExtBarcode(),
                product.getIntBarcode(),
                product.getAbcID()
        );
        productRepository.save(productEntity);
    }

    @Override
    public void updateProduct(Product product) {
        ProductEntity productEntity = new ProductEntity(
                product.getProductID(),
                product.getProductName(),
                product.getProductCode(),
                product.getDescription(),
                product.getWeight(),
                product.getVolume(),
                product.getExpirationDateRequired(),
                product.getDel(),
                product.getSupplierID(),
                product.getCategoryID(),
                product.getExtBarcode(),
                product.getIntBarcode(),
                product.getAbcID()
        );
        productRepository.save(productEntity);
    }

    @Override
    public void delProduct(Product product) {
        ProductEntity productEntity = new ProductEntity(
                product.getProductID(),
                product.getProductName(),
                product.getProductCode(),
                product.getDescription(),
                product.getWeight(),
                product.getVolume(),
                product.getExpirationDateRequired(),
                false,
                product.getSupplierID(),
                product.getCategoryID(),
                product.getExtBarcode(),
                product.getIntBarcode(),
                product.getAbcID()
        );
        productRepository.save(productEntity);
    }
}
