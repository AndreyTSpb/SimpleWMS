package com.tynyany.simplewmsv2.exception;

/**
 * Нарушение бизнес-правила склада (недостаточно остатка, неверный статус документа и т.п.).
 * Сообщение предназначено для пользователя.
 */
public class WmsException extends RuntimeException {
    public WmsException(String message) {
        super(message);
    }
}
