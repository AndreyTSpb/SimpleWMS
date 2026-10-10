package com.tynyany.simplewmsv2.domain;

import lombok.Getter;

/**
 * Статус маршрута комплектации (сборки заказа). Числовой код хранится в picking_route.status_id.
 */
@Getter
public enum PickingRouteStatus {
    CREATED(0, "Создан"),
    IN_PROGRESS(1, "В работе"),
    COMPLETED(2, "Завершён"),
    CANCELLED(3, "Отменён");

    private final int code;
    private final String title;

    PickingRouteStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public static PickingRouteStatus fromCode(int code) {
        for (PickingRouteStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Неизвестный статус маршрута комплектации: " + code);
    }
}
