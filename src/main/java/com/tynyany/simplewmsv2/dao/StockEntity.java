package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="stocks")
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int stockId;
    private int product_id; //Ссылка на товар.
    private int batchId; //Ссылка на партию
    private int locationId; //Ссылка на местоположение
    private int quantity; //Количество товара в данной партии в данном местоположении
    private Boolean del;
}
