package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.domain.ReceivingStatus;
import com.tynyany.simplewmsv2.dto.erp.ApiResponse;
import com.tynyany.simplewmsv2.dto.erp.ErpCustomer;
import com.tynyany.simplewmsv2.dto.erp.ErpOrder;
import com.tynyany.simplewmsv2.dto.erp.ErpProduct;
import com.tynyany.simplewmsv2.dto.erp.ErpReceiving;
import com.tynyany.simplewmsv2.dto.erp.ErpSupplier;
import com.tynyany.simplewmsv2.dto.erp.ErpViews;
import com.tynyany.simplewmsv2.exception.BadRequestException;
import com.tynyany.simplewmsv2.service.ErpService;
import com.tynyany.simplewmsv2.service.OrderService;
import com.tynyany.simplewmsv2.service.ReceivingProcessService;
import com.tynyany.simplewmsv2.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST API для ERP: справочники, приходы, заказы, отгрузки. Описание — /swagger-ui.html.
 * Доступ: заголовок X-API-Key или сессия администратора / менеджера.
 */
@RestController
@RequestMapping(path = "/api/v1", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@SecurityRequirement(name = "apiKey")
public class ErpApiController {

    private final ErpService erpService;
    private final ReceivingProcessService receivingProcessService;
    private final OrderService orderService;
    private final ShippingService shippingService;

    // ---------- справочники ----------

    @Tag(name = "Справочники")
    @Operation(summary = "Загрузить товары", description = "Создание или обновление по коду. Поставщик должен быть в справочнике; "
            + "категория ищется по названию и создаётся при отсутствии. Сохраняются все записи или ни одной.")
    @PutMapping("/products")
    public ApiResponse<ErpViews.UpsertResult> products(@RequestBody @NotEmpty List<@Valid ErpProduct> products) {
        ErpViews.UpsertResult result = erpService.upsertProducts(products);
        return ApiResponse.ok("Товары: создано " + result.created() + ", обновлено " + result.updated(), result);
    }

    @Tag(name = "Справочники")
    @Operation(summary = "Загрузить поставщиков", description = "Создание или обновление по коду.")
    @PutMapping("/suppliers")
    public ApiResponse<ErpViews.UpsertResult> suppliers(@RequestBody @NotEmpty List<@Valid ErpSupplier> suppliers) {
        ErpViews.UpsertResult result = erpService.upsertSuppliers(suppliers);
        return ApiResponse.ok("Поставщики: создано " + result.created() + ", обновлено " + result.updated(), result);
    }

    @Tag(name = "Справочники")
    @Operation(summary = "Загрузить клиентов", description = "Создание или обновление по коду.")
    @PutMapping("/customers")
    public ApiResponse<ErpViews.UpsertResult> customers(@RequestBody @NotEmpty List<@Valid ErpCustomer> customers) {
        ErpViews.UpsertResult result = erpService.upsertCustomers(customers);
        return ApiResponse.ok("Клиенты: создано " + result.created() + ", обновлено " + result.updated(), result);
    }

    // ---------- приход ----------

    @Tag(name = "Приход")
    @Operation(summary = "Загрузить приходную накладную", description = "Накладная создаётся в статусе «В ожидании». "
            + "Товары, которых нет в справочнике, пропускаются и перечисляются в missingProducts. "
            + "Номер не должен совпадать с неотменённой накладной.")
    @PostMapping("/receivings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ErpViews.DocumentCreated> createReceiving(@RequestBody @Valid ErpReceiving request) {
        ReceivingProcessService.ErpImportResult result = receivingProcessService.createFromErp(request);
        return ApiResponse.ok("Накладная " + request.number().trim() + " загружена", new ErpViews.DocumentCreated(
                result.receivingId(), request.number().trim(), ReceivingStatus.WAITING.getTitle(),
                result.linesCreated(), result.missingProductCodes(), null));
    }

    @Tag(name = "Приход")
    @Operation(summary = "Статус и результаты приёмки накладной")
    @GetMapping("/receivings/{number}")
    public ApiResponse<ErpViews.Receiving> receiving(@PathVariable("number") String number) {
        return ApiResponse.ok("Накладная " + number, erpService.getReceiving(number));
    }

    @Tag(name = "Приход")
    @Operation(summary = "Выгрузка завершённых приёмок", description = "Накладные в статусе «Завершена», приёмка которых начата не раньше from.")
    @GetMapping("/receivings")
    public ApiResponse<List<ErpViews.Receiving>> completedReceivings(
            @Parameter(description = "ГГГГ-ММ-ДД, по умолчанию — 30 дней назад", example = "2026-10-01")
            @RequestParam(value = "from", required = false) String from) {
        List<ErpViews.Receiving> result = erpService.getCompletedReceivings(parseFrom(from));
        return ApiResponse.ok("Завершённых приёмок: " + result.size(), result);
    }

    // ---------- заказы ----------

    @Tag(name = "Заказы")
    @Operation(summary = "Загрузить заказ клиента", description = "Заказ создаётся и сразу резервируется по FEFO. "
            + "Если товара не хватает — статус «Частично зарезервирован», недостача в shortage.")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ErpViews.DocumentCreated> createOrder(@RequestBody @Valid ErpOrder request) {
        OrderService.ErpOrderResult result = orderService.createFromErp(request);
        return ApiResponse.ok("Заказ " + request.number().trim() + " загружен", new ErpViews.DocumentCreated(
                result.orderId(), request.number().trim(), result.reserve().status(), result.linesCreated(),
                result.missingProductCodes(), result.reserve().shortage()));
    }

    @Tag(name = "Заказы")
    @Operation(summary = "Статус заказа и выполнение по строкам")
    @GetMapping("/orders/{number}")
    public ApiResponse<ErpViews.Order> order(@PathVariable("number") String number) {
        return ApiResponse.ok("Заказ " + number, erpService.getOrder(number));
    }

    @Tag(name = "Заказы")
    @Operation(summary = "Отменить заказ", description = "Возможно до передачи в сборку; резерв снимается.")
    @PostMapping("/orders/{number}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable("number") String number) {
        erpService.cancelOrder(number);
        return ApiResponse.ok("Заказ " + number + " отменён");
    }

    // ---------- отгрузки ----------

    @Tag(name = "Отгрузки")
    @Operation(summary = "Выгрузка отгрузок", description = "Отгрузки начиная с даты from; строки сгруппированы по коду товара.")
    @GetMapping("/shipments")
    public ApiResponse<List<ShippingService.ErpShipment>> shipments(
            @Parameter(description = "ГГГГ-ММ-ДД, по умолчанию — 30 дней назад", example = "2026-10-01")
            @RequestParam(value = "from", required = false) String from) {
        List<ShippingService.ErpShipment> result = shippingService.getForErp(parseFrom(from));
        return ApiResponse.ok("Отгрузок: " + result.size(), result);
    }

    @Tag(name = "Отгрузки")
    @Operation(summary = "Подтвердить получение отгрузки", description = "Отгрузка отмечается как переданная в ERP.")
    @PostMapping("/shipments/{id}/confirm")
    public ApiResponse<Void> confirmShipment(@PathVariable("id") int id) {
        shippingService.confirmExport(id);
        return ApiResponse.ok("Получение отгрузки " + shippingService.getShipment(id).number() + " подтверждено");
    }

    private static LocalDate parseFrom(String from) {
        if (from == null || from.isBlank()) return LocalDate.now().minusDays(30);
        try {
            return LocalDate.parse(from.trim());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неверный формат даты (ожидается ГГГГ-ММ-ДД): " + from);
        }
    }
}
