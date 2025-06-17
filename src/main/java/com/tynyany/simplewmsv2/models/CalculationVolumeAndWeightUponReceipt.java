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
                this.volume += receivingLine.getQuantityProduct()*this.product(receivingLine.getReceivingLineID()).getVolume();
                this.weight += receivingLine.getQuantityProduct()*this.product(receivingLine.getReceivingLineID()).getWeight();
            }
        }
    }

    private Product product(Integer productID){
        return productService.getProductByID(productID);
    }
}
