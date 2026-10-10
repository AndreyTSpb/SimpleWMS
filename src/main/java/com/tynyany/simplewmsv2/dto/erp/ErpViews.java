package com.tynyany.simplewmsv2.dto.erp;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Ответы API о документах склада.
 */
public final class ErpViews {

    private ErpViews() {
    }

    @Schema(description = "Результат загрузки справочника")
    public record UpsertResult(@Schema(example = "2") int created, @Schema(example = "3") int updated) {}

    @Schema(description = "Результат загрузки документа")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocumentCreated(
            @Schema(example = "12") int id,
            @Schema(example = "З30016546") String number,
            @Schema(example = "В ожидании") String status,
            @Schema(example = "5") int lines,
            @Schema(description = "Коды товаров, которых нет в справочнике (строки пропущены)") List<String> missingProducts,
            @Schema(description = "Для заказа: не хватило к резерву, шт.", example = "0") Integer shortage) {}

    @Schema(description = "Строка приходной накладной")
    public record ReceivingLine(
            @Schema(example = "029952") String code,
            @Schema(example = "Карандаш чернографитный HB") String name,
            @Schema(example = "200") int expected,
            @Schema(example = "198") int received,
            @Schema(description = "Строка принята", example = "true") boolean accepted,
            @Schema(description = "Срок годности, ГГГГ-ММ-ДД", example = "2027-06-30") String expiryDate) {}

    @Schema(description = "Приходная накладная: статус и результаты приёмки")
    public record Receiving(
            @Schema(example = "12") int id,
            @Schema(example = "З30016546") String number,
            @Schema(example = "П000019") String supplierCode,
            @Schema(example = "2026-10-10 08:30:00") String date,
            @Schema(example = "Завершена") String status,
            @Schema(description = "Начало приёмки", example = "2026-10-10 10:15:00") String receivedAt,
            List<ReceivingLine> lines) {}

    @Schema(description = "Строка заказа")
    public record OrderLine(
            @Schema(example = "029952") String code,
            @Schema(example = "Карандаш чернографитный HB") String name,
            @Schema(example = "20") int ordered,
            @Schema(example = "20") int reserved,
            @Schema(example = "18") int picked,
            @Schema(example = "18") int shipped) {}

    @Schema(description = "Заказ клиента: статус и выполнение по строкам")
    public record Order(
            @Schema(example = "7") int id,
            @Schema(example = "ПР-100") String number,
            @Schema(example = "К000001") String customerCode,
            @Schema(example = "2026-10-10 09:00:00") String date,
            @Schema(example = "2026-10-11") String shipDate,
            @Schema(example = "Отгружен") String status,
            @Schema(description = "Номер документа отгрузки", example = "ОТГ-3") String shipment,
            List<OrderLine> lines) {}
}
