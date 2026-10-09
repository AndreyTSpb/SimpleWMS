package com.tynyany.simplewmsv2.exception;

/**
 * Запрошенная запись не найдена.
 */
public class NotFoundException extends WmsException {
    public NotFoundException(String message) {
        super(message);
    }
}
