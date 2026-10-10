package com.tynyany.simplewmsv2.dto.erp;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ошибка в конкретном поле запроса.
 */
@Schema(description = "Ошибка поля запроса")
public record ApiError(
        @Schema(example = "lines[0].quantity") String field,
        @Schema(example = "должно быть больше 0") String message) {
}
