package com.tynyany.simplewmsv2.dto.erp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Заказ клиента из ERP. После создания сразу резервируется по FEFO.
 */
@Schema(description = "Заказ клиента")
public record ErpOrder(
        @Schema(description = "Номер заказа в ERP", example = "ПР-100") @NotBlank @Size(max = 50) String number,
        @Schema(description = "Код клиента", example = "К000001") @NotBlank String customerCode,
        @Schema(description = "Дата заказа, ГГГГ-ММ-ДД ЧЧ:ММ:СС (пусто — текущая)", example = "2026-10-10 09:00:00", type = "string")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date,
        @Schema(description = "Плановая дата отгрузки, ГГГГ-ММ-ДД", example = "2026-10-11", type = "string")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate shipDate,
        @Schema(example = "Доставка до 12:00") @Size(max = 255) String note,
        @NotEmpty @Valid List<ErpLine> lines) {
}
