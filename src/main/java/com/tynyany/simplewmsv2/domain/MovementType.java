package com.tynyany.simplewmsv2.domain;

import lombok.Getter;

/**
 * Тип движения товара по складу.
 * Числовой код хранится в stock_movements.movement_type_id.
 */
@Getter
public enum MovementType {
    EXPECTED(0, "Ожидаем"),
    RECEIVING(1, "Приемка"),
    PLACEMENT(2, "Размещение"),
    PICKING(3, "Сборка"),
    SHIPPING(4, "Отгрузка"),
    ADJUSTMENT(5, "Корректирование"),
    CYCLE_COUNT(6, "Инвентаризация");

    private final int code;
    private final String title;

    MovementType(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public static MovementType fromCode(int code) {
        for (MovementType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Неизвестный тип движения: " + code);
    }
}
