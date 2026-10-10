package com.tynyany.simplewmsv2.dto.erp;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Единый формат ответа REST API: success, сообщение, данные или список ошибок полей.
 */
@Schema(description = "Ответ API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @Schema(example = "true") boolean success,
        @Schema(example = "Готово") String message,
        T data,
        List<ApiError> errors) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    public static ApiResponse<Void> error(String message, List<ApiError> errors) {
        return new ApiResponse<>(false, message, null, errors == null || errors.isEmpty() ? null : errors);
    }
}
