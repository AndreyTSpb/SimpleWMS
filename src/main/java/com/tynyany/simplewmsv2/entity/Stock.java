package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Stock {
    int stockId;
    int product_id; //Ссылка на товар.
    int batchId; //Ссылка на партию
    int locationId; //Ссылка на местоположение
    int quantity; //Количество товара в данной партии в данном местоположении
    Boolean del;
}
