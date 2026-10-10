package com.tynyany.simplewmsv2.it;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * База данных для интеграционных тестов.
 * <p>
 * По умолчанию поднимается контейнер PostgreSQL 16 (Testcontainers) — один на весь прогон тестов.
 * Если задан адрес внешней базы (системное свойство {@code wms.test.jdbc-url} или переменная окружения
 * {@code WMS_TEST_JDBC_URL}), используется он — так тесты запускаются там, где нет Docker.
 * Без Docker и без внешней базы интеграционные тесты пропускаются.
 */
public final class TestDatabase {

    private static final String EXTERNAL_URL = setting("wms.test.jdbc-url", "WMS_TEST_JDBC_URL");
    private static PostgreSQLContainer<?> container;

    private TestDatabase() {
    }

    /** Условие запуска интеграционных тестов (для {@code @EnabledIf}). */
    public static boolean available() {
        if (EXTERNAL_URL != null) return true;
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable e) {
            return false;
        }
    }

    static synchronized void register(DynamicPropertyRegistry registry) {
        if (EXTERNAL_URL != null) {
            registry.add("spring.datasource.url", () -> EXTERNAL_URL);
            registry.add("spring.datasource.username", () -> valueOr(setting("wms.test.username", "WMS_TEST_USERNAME"), "admin"));
            registry.add("spring.datasource.password", () -> valueOr(setting("wms.test.password", "WMS_TEST_PASSWORD"), "password"));
            return;
        }
        if (container == null) {
            container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("wms_test")
                    .withUsername("wms")
                    .withPassword("wms");
            container.start();
        }
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    private static String setting(String property, String env) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(env);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String valueOr(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
