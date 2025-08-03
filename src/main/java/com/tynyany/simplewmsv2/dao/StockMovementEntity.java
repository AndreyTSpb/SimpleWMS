package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="stock_movements")
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movementId; //Уникальный идентификатор перемещения
    private int batchId; //Ссылка на партию
    private int fromLocationId; //Ссылка на местоположение ОТКУДА товар был перемещен. Может быть NULL при приемке
    private int toLocationId; //Ссылка на местоположение КУДА товар был перемещен. Может быть NULL при отгрузке.
    private int quantity; //Количество перемещенного товара
    private Timestamp movementDate; //Дата и время перемещения
    private int employeeID; //
    private int movementTypeId; //array('Ожидаем', 'Приемка', 'Размещение', 'Сборка', 'Отгрузка', 'Корректирование', 'CycleCount') - Тип перемещения. Определяет, что произошло с товаром.
    private int shipmentItemId; //Ссылка на строку отгрузки, если перемещение связано с отгрузкой. Может быть NULL.
    private Boolean deleted;
}
