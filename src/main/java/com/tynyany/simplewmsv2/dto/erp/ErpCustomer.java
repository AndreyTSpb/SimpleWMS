package com.tynyany.simplewmsv2.dto.erp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Клиент из ERP. Создаётся или обновляется по коду.
 */
@Schema(description = "Клиент (создаётся или обновляется по коду)")
public record ErpCustomer(
        @Schema(example = "К000001") @NotBlank @Size(max = 50) String code,
        @Schema(example = "ООО «Демо-Клиент»") @NotBlank @Size(max = 255) String name,
        @Schema(example = "+7 812 000-00-02") String phone,
        @Schema(example = "client@example.com") @Email String email,
        @Schema(example = "190000, г. Санкт-Петербург, ул. Примерная, д. 1") String address,
        @Schema(description = "Начало приёма товара, ЧЧ:ММ", example = "09:00")
        @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "ожидается время ЧЧ:ММ") String workingFrom,
        @Schema(description = "Окончание приёма товара, ЧЧ:ММ", example = "18:00")
        @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "ожидается время ЧЧ:ММ") String workingTo) {
}
