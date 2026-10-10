package com.tynyany.simplewmsv2.it;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Базовый класс интеграционных тестов: полный контекст приложения на настоящей PostgreSQL
 * (см. {@link TestDatabase}), профиль test, MockMvc для HTTP-запросов.
 * Контекст и база общие для всех тестов прогона, поэтому каждый тест использует свои номера документов и коды.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@EnabledIfTestDatabase
public abstract class IntegrationTest {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        TestDatabase.register(registry);
    }
}
