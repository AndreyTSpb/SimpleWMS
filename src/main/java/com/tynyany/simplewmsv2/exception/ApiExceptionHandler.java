package com.tynyany.simplewmsv2.exception;

import com.tynyany.simplewmsv2.controller.ErpApiController;
import com.tynyany.simplewmsv2.dto.erp.ApiError;
import com.tynyany.simplewmsv2.dto.erp.ApiResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * Ошибки REST API в едином формате {@link ApiResponse}: 400 — неверные данные, 404 — не найдено,
 * 409 — нарушение бизнес-правила (дубль, недопустимый статус), 500 — внутренняя ошибка.
 */
@Slf4j
@ControllerAdvice(assignableTypes = ErpApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(MethodArgumentNotValidException ex) {
        List<ApiError> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ApiError(error.getField(), error.getDefaultMessage()));
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.add(new ApiError(error.getObjectName(), error.getDefaultMessage())));
        return validationFailed(errors);
    }

    /** Ошибки проверки элементов списка (List&lt;@Valid ...&gt;) и параметров метода. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException ex) {
        List<ApiError> errors = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String prefix = result.getContainerIndex() != null ? "[" + result.getContainerIndex() + "]" : "";
            if (result instanceof ParameterErrors parameterErrors) {
                for (FieldError error : parameterErrors.getFieldErrors()) {
                    errors.add(new ApiError(prefix + "." + error.getField(), error.getDefaultMessage()));
                }
            } else {
                String name = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error ->
                        errors.add(new ApiError(name != null ? name + prefix : prefix, error.getDefaultMessage())));
            }
        });
        return validationFailed(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof com.fasterxml.jackson.core.JsonProcessingException)) {
            cause = cause.getCause();
        }
        String message = describeUnreadable(cause);
        log.warn("API: некорректный JSON: {}", message);
        return body(HttpStatus.BAD_REQUEST, message, null);
    }

    /** Понятное описание ошибки разбора JSON: синтаксис (строка и позиция) или неверное значение поля. */
    static String describeUnreadable(Throwable cause) {
        if (cause instanceof JsonParseException parse && parse.getLocation() != null) {
            return "Некорректный JSON: строка " + parse.getLocation().getLineNr()
                    + ", позиция " + parse.getLocation().getColumnNr();
        }
        if (cause instanceof JsonMappingException mapping && !mapping.getPath().isEmpty()) {
            StringBuilder field = new StringBuilder();
            for (JsonMappingException.Reference ref : mapping.getPath()) {
                if (ref.getIndex() >= 0) {
                    field.append('[').append(ref.getIndex()).append(']');
                } else if (ref.getFieldName() != null) {
                    if (!field.isEmpty()) field.append('.');
                    field.append(ref.getFieldName());
                }
            }
            String value = cause instanceof InvalidFormatException invalid && invalid.getValue() != null
                    ? " «" + invalid.getValue() + "»" : "";
            return "Неверное значение поля " + field + value + " (проверьте тип и формат: числа — без кавычек, "
                    + "даты — ГГГГ-ММ-ДД или ГГГГ-ММ-ДД ЧЧ:ММ:СС)";
        }
        return "Тело запроса пустое или не является JSON";
    }

    @ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        log.warn("API: неверный запрос: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, ex instanceof BadRequestException ? ex.getMessage()
                : "Неверное значение параметра: " + ((MethodArgumentTypeMismatchException) ex).getName(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Метод " + ex.getMethod() + " не поддерживается", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Ожидается Content-Type: application/json", null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        log.warn("API: не найдено: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(WmsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(WmsException ex) {
        log.warn("API: нарушение бизнес-правила: {}", ex.getMessage());
        return body(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        log.error("API: внутренняя ошибка", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", null);
    }

    private ResponseEntity<ApiResponse<Void>> validationFailed(List<ApiError> errors) {
        log.warn("API: ошибки проверки данных: {}", errors);
        return body(HttpStatus.BAD_REQUEST, "Ошибки в данных запроса", errors);
    }

    private ResponseEntity<ApiResponse<Void>> body(HttpStatus status, String message, List<ApiError> errors) {
        return ResponseEntity.status(status)
                .contentType(new MediaType(MediaType.APPLICATION_JSON, java.nio.charset.StandardCharsets.UTF_8))
                .body(ApiResponse.error(message, errors));
    }
}
