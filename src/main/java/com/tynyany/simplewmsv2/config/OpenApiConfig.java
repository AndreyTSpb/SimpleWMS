package com.tynyany.simplewmsv2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Описание REST API для Swagger UI (/swagger-ui.html). Схема авторизации — заголовок X-API-Key.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI simpleWmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SimpleWMS — API для ERP")
                        .version("v1")
                        .description("Обмен с учётной системой: справочники, приходные накладные, заказы клиентов, "
                                + "выгрузка результатов приёмки и отгрузки. Все ответы — в формате "
                                + "{success, message, data, errors}. Коды: 400 — неверные данные, 401 — нет ключа, "
                                + "404 — не найдено, 409 — дубль номера или недопустимый статус."))
                .components(new Components().addSecuritySchemes("apiKey", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("Ключ доступа ERP (параметр wms.api.key, в dev — dev-api-key)")));
    }
}
