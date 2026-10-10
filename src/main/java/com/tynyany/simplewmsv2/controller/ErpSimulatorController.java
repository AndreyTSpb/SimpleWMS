package com.tynyany.simplewmsv2.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Имитация ERP: страница отправляет готовые примеры запросов в REST API /api/v1 от имени пользователя
 * и показывает ответ. Примеры лежат в resources/erp-samples (те же, что в Project_Docs/api).
 */
@Controller
@RequestMapping("/erp_simulator")
public class ErpSimulatorController {

    /** Операция ERP: HTTP-метод, адрес и пример тела запроса (для GET — пусто). */
    public record Operation(String key, String group, String title, String method, String url, String body) {}

    @GetMapping
    public String index(Model model) {
        String monthAgo = LocalDate.now().minusDays(30).toString();
        model.addAttribute("title", "Имитация ERP");
        model.addAttribute("operations", List.of(
                new Operation("suppliers", "Справочники", "Загрузить поставщиков", "PUT", "/api/v1/suppliers", sample("suppliers")),
                new Operation("customers", "Справочники", "Загрузить клиентов", "PUT", "/api/v1/customers", sample("customers")),
                new Operation("products", "Справочники", "Загрузить товары", "PUT", "/api/v1/products", sample("products")),
                new Operation("receiving", "Приход", "Загрузить приходную накладную", "POST", "/api/v1/receivings", sample("receiving")),
                new Operation("receivingStatus", "Приход", "Статус накладной", "GET", "/api/v1/receivings/З-ДЕМО-1", ""),
                new Operation("receivings", "Приход", "Выгрузить завершённые приёмки", "GET", "/api/v1/receivings?from=" + monthAgo, ""),
                new Operation("order", "Заказы", "Загрузить заказ", "POST", "/api/v1/orders", sample("order")),
                new Operation("orderStatus", "Заказы", "Статус заказа", "GET", "/api/v1/orders/ПР-ДЕМО-1", ""),
                new Operation("orderCancel", "Заказы", "Отменить заказ", "POST", "/api/v1/orders/ПР-ДЕМО-1/cancel", ""),
                new Operation("shipments", "Отгрузки", "Выгрузить отгрузки", "GET", "/api/v1/shipments?from=" + monthAgo, ""),
                new Operation("shipmentConfirm", "Отгрузки", "Подтвердить получение отгрузки", "POST", "/api/v1/shipments/1/confirm", "")));
        return "erp_simulator";
    }

    private static String sample(String name) {
        try (InputStream in = new ClassPathResource("erp-samples/" + name + ".json").getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не найден пример запроса " + name, e);
        }
    }
}
