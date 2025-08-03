package com.tynyany.simplewmsv2.entity;

import lombok.Value;

import java.sql.Timestamp;

/**
 * История перемещений
 */
@Value
public class StockMovement {
    int movementId; //Уникальный идентификатор перемещения
    int batchId; //Ссылка на партию
    int fromLocationId; //Ссылка на местоположение ОТКУДА товар был перемещен. Может быть NULL при приемке
    int toLocationId; //Ссылка на местоположение КУДА товар был перемещен. Может быть NULL при отгрузке.
    int quantity; //Количество перемещенного товара
    Timestamp movementDate; //Дата и время перемещения
    int employeeID; //
    int movementTypeId; //array('Ожидаем', 'Приемка', 'Размещение', 'Сборка', 'Отгрузка', 'Корректирование', 'CycleCount') - Тип перемещения. Определяет, что произошло с товаром.
    int shipmentItemId; //Ссылка на строку отгрузки, если перемещение связано с отгрузкой. Может быть NULL.
    Boolean del;
}
