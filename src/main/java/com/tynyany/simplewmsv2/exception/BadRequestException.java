package com.tynyany.simplewmsv2.exception;

/**
 * Некорректные параметры запроса (в REST API — ответ 400).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
