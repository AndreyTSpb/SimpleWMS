package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="batches")
public class BatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int batch_id; //Уникальный идентификатор партии.
    private int product_id; //Ссылка на товар.
    private int supplier_id; //Ссылка на поставщика.
    private int date_received; //Дата поступления партии
    private int quantity_received; //Количество товара в партии при поступлении
    private int expiry_date; //Срок годности (если есть)
    private int receiving_id; //номер накладной поставщика
    private Boolean del = false;
}
