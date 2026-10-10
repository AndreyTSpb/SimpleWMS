package com.tynyany.simplewmsv2.dto.erp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Приходная накладная (ожидаемая поставка) из ERP.
 */
@Schema(description = "Приходная накладная")
public record ErpReceiving(
        @Schema(description = "Номер документа в ERP", example = "З30016546") @NotBlank @Size(max = 50) String number,
        @Schema(description = "Код поставщика", example = "П000019") @NotBlank String supplierCode,
        @Schema(description = "Дата документа, ГГГГ-ММ-ДД ЧЧ:ММ:СС", example = "2026-10-10 08:30:00", type = "string")
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date,
        @NotEmpty @Valid List<ErpLine> lines) {
}
