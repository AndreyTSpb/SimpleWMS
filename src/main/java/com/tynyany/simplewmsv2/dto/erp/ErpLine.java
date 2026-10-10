package com.tynyany.simplewmsv2.dto.erp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Строка документа ERP: код товара и количество.
 */
@Schema(description = "Строка документа")
public record ErpLine(
        @Schema(description = "Код товара", example = "029952") @NotBlank String code,
        @Schema(description = "Количество, шт.", example = "20") @Positive int quantity) {
}
