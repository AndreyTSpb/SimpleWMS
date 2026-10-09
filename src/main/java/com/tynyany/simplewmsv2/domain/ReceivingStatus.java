package com.tynyany.simplewmsv2.domain;

import lombok.Getter;

/**
 * Статус приходной накладной.
 * Числовой код хранится в receiving.statusid и используется в шаблонах.
 */
@Getter
public enum ReceivingStatus {
    WAITING(0, "В ожидании"),
    IN_PROGRESS(1, "В процессе"),
    COMPLETED(2, "Завершена"),
    CANCELLED(3, "Отменена");

    private final int code;
    private final String title;

    ReceivingStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public static ReceivingStatus fromCode(int code) {
        for (ReceivingStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Неизвестный статус прихода: " + code);
    }
}
