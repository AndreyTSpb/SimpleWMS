package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.Product;
import com.tynyany.simplewmsv2.entity.ReceivingLine;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.service.ProductService;
import com.tynyany.simplewmsv2.service.ReceivingLineService;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public class CalculationVolumeAndWeightUponReceipt {
    private Integer receiptId;
    @Getter
    private float volume = 0;
    @Getter
    private float weight = 0;
    @Getter
    private Integer qntProducts = 0;
    private final ReceivingLineService receivingLineService;
    private final ProductService productService;


    public CalculationVolumeAndWeightUponReceipt(Integer receiptId, ReceivingLineService receivingLineService, ProductService productService) {
        this.receiptId = receiptId;
        this.receivingLineService = receivingLineService;
        this.productService = productService;
        this.calculation();
    }

    private void calculation(){

        List<ReceivingLine> receivingLineList = receivingLineService.getAllLinesForReceiving(this.receiptId);
        if(receivingLineList != null){
            for(ReceivingLine receivingLine : receivingLineList){

                this.qntProducts ++;
                Product product = this.product(receivingLine.getProductID());
                if (product == null) continue;
                this.volume += receivingLine.getQuantityProduct() * (product.getVolume() != null ? product.getVolume() : 0);
                this.weight += receivingLine.getQuantityProduct() * (product.getWeight() != null ? product.getWeight() : 0);
            }
        }
    }

    private Product product(Integer productID){
        return productService.getProductByID(productID);
    }
}
