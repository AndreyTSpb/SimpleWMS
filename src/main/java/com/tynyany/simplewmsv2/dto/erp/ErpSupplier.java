package com.tynyany.simplewmsv2.dto.erp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Поставщик из ERP. Создаётся или обновляется по коду.
 */
@Schema(description = "Поставщик (создаётся или обновляется по коду)")
public record ErpSupplier(
        @Schema(example = "П000019") @NotBlank @Size(max = 50) String code,
        @Schema(example = "ООО «Демо-Поставщик»") @NotBlank @Size(max = 255) String name,
        @Schema(example = "Сидоров С. С.") String contactPerson,
        @Schema(example = "+7 812 000-00-01") String phone,
        @Schema(example = "supply@example.com") @Email String email) {
}
