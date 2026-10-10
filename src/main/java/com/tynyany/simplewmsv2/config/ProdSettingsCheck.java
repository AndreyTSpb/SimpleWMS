package com.tynyany.simplewmsv2.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Проверка настроек при запуске в профиле prod (см. Project_Docs/Развёртывание.md):
 * без ключа API приложение не стартует, учебный ключ разработки — предупреждение в журнале.
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProdSettingsCheck {

    static final String DEV_KEY = "dev-api-key";

    private final WmsProperties properties;

    @PostConstruct
    void check() {
        String key = properties.getApi().getKey();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Профиль prod: не задан ключ REST API — переменная WMS_API_KEY в файле .env");
        }
        if (DEV_KEY.equals(key.trim())) {
            log.warn("Профиль prod: используется ключ API разработки ({}). Задайте свой WMS_API_KEY в .env", DEV_KEY);
        }
        log.info("Профиль prod: склад «{}», ключ API задан", properties.getCompany().getName());
    }
}
