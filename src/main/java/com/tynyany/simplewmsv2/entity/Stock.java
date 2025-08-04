package com.tynyany.simplewmsv2.entity;

import lombok.Data;


@Data

public class Stock {
    private final int stockId;
    private final int product_id; //Ссылка на товар.
    private final int batchId; //Ссылка на партию
    private final int locationId; //Ссылка на местоположение
    private int quantity; //Количество товара в данной партии в данном местоположении
    private Boolean del;

    public Stock(int stockId, int product_id, int batchId, int locationId, int quantity, Boolean del){
        this.stockId = stockId;
        this.product_id = product_id;
        this.batchId = batchId;
        this.locationId = locationId;

        this.quantity = quantity;
        this.del = del;
    }
}
