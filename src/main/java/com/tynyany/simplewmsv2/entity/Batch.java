package com.tynyany.simplewmsv2.entity;

import lombok.Value;

import java.sql.Timestamp;

/**
 * Партия товара
 */
@Value
public class Batch {
    int batch_id; //Уникальный идентификатор партии.
    int product_id; //Ссылка на товар.
    int supplier_id; //Ссылка на поставщика.
    Timestamp date_received; //Дата поступления партии
    int quantity_received; //Количество товара в партии при поступлении
    Timestamp expiry_date; //Срок годности (если есть)
    int receiving_id; //номер строки накладной поставщика
    Boolean del;

    public Batch(int batchId, int productId, int supplierId, Timestamp dateReceived, int quantityReceived, Timestamp expiryDate, int receivingId, Boolean del) {

        this.batch_id = batchId;
        this.product_id = productId;
        this.supplier_id = supplierId;
        this.date_received = dateReceived;
        this.quantity_received = quantityReceived;
        this.expiry_date = expiryDate;
        receiving_id = receivingId;
        this.del = del;
    }
}
