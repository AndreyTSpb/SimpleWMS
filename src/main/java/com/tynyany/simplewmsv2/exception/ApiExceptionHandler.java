package com.tynyany.simplewmsv2.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tynyany.simplewmsv2.controller.RestApiController;
import com.tynyany.simplewmsv2.entity.ResponseJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Ошибки REST API возвращаются в формате JSON: {"success": 0, "message": "..."}.
 */
@Slf4j
@ControllerAdvice(assignableTypes = RestApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseJson> handleNotFound(NotFoundException ex) {
        log.warn("API: не найдено: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WmsException.class)
    public ResponseEntity<ResponseJson> handleBusiness(WmsException ex) {
        log.warn("API: нарушение бизнес-правила: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ResponseJson> handleBadJson(JsonProcessingException ex) {
        log.warn("API: некорректный JSON: {}", ex.getOriginalMessage());
        return body(HttpStatus.BAD_REQUEST, "Некорректный JSON: " + ex.getOriginalMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseJson> handleOther(Exception ex) {
        log.error("API: внутренняя ошибка", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ResponseJson> body(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(new MediaType(MediaType.APPLICATION_JSON, java.nio.charset.StandardCharsets.UTF_8))
                .body(new ResponseJson(0, message));
    }
}
