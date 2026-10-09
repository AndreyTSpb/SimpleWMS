package com.tynyany.simplewmsv2.domain;

import lombok.Getter;

/**
 * Статус маршрута размещения.
 * Числовой код хранится в placement_route.status.
 */
@Getter
public enum PlacementRouteStatus {
    CREATED(0, "Создан"),
    IN_PROGRESS(1, "В работе"),
    COMPLETED(2, "Завершён"),
    CANCELLED(3, "Отменён");

    private final int code;
    private final String title;

    PlacementRouteStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public static PlacementRouteStatus fromCode(int code) {
        for (PlacementRouteStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Неизвестный статус маршрута: " + code);
    }
}
