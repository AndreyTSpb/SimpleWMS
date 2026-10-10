package com.tynyany.simplewmsv2.it;

import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Тест выполняется, только если доступна база (Docker или внешняя база, см. {@link TestDatabase}).
 * Аннотация наследуется, поэтому условие действует на все наследники {@link IntegrationTest}
 * (сама {@code @EnabledIf} не наследуется).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnabledIf(value = "com.tynyany.simplewmsv2.it.TestDatabase#available",
        disabledReason = "Нет Docker и не задана внешняя база WMS_TEST_JDBC_URL")
public @interface EnabledIfTestDatabase {
}
