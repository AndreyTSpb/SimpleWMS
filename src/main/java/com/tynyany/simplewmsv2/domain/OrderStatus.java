package com.tynyany.simplewmsv2.domain;

import lombok.Getter;

/**
 * Статус заказа клиента. Числовой код хранится в customer_order.status_id.
 */
@Getter
public enum OrderStatus {
    NEW(0, "Новый"),
    PARTIALLY_RESERVED(1, "Частично зарезервирован"),
    RESERVED(2, "Зарезервирован"),
    PICKING(3, "В сборке"),
    PICKED(4, "Собран"),
    SHIPPED(5, "Отгружен"),
    CANCELLED(6, "Отменён");

    private final int code;
    private final String title;

    OrderStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    /** Заказ ещё можно менять, резервировать и отменять (не передан в сборку). */
    public boolean isEditable() {
        return this == NEW || this == PARTIALLY_RESERVED || this == RESERVED;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Неизвестный статус заказа: " + code);
    }
}
