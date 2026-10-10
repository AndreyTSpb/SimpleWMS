package com.tynyany.simplewmsv2.dto.erp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Товар из ERP. Создаётся или обновляется по коду.
 */
@Schema(description = "Товар (создаётся или обновляется по коду)")
public record ErpProduct(
        @Schema(example = "029952") @NotBlank @Size(max = 50) String code,
        @Schema(example = "Карандаш чернографитный HB") @NotBlank @Size(max = 255) String name,
        @Schema(example = "") String description,
        @Schema(description = "Код поставщика из справочника", example = "П000019") @NotBlank String supplierCode,
        @Schema(description = "Категория (по названию; новая создаётся)", example = "Письменные принадлежности") String category,
        @Schema(description = "ABC-класс", example = "B") @Pattern(regexp = "[ABCDabcd]?", message = "должен быть A, B, C или D") String abc,
        @Schema(description = "Вес единицы, кг", example = "0.01") @PositiveOrZero Float weight,
        @Schema(description = "Объём единицы, л", example = "0.01") @PositiveOrZero Float volume,
        @Schema(description = "Учитывать срок годности", example = "false") Boolean expirationRequired,
        @Schema(example = "4600000029952") String extBarcode,
        @Schema(example = "2000000029952") String intBarcode) {
}
