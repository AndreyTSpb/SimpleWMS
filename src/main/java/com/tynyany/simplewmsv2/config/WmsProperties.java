package com.tynyany.simplewmsv2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Настройки склада (префикс wms в application.properties).
 */
@Data
@Component
@ConfigurationProperties(prefix = "wms")
public class WmsProperties {

    private Locations locations = new Locations();

    /**
     * Системные ячейки. Создаются скриптом SQL/migrations/V001__seed_reference_data.sql.
     */
    @Data
    public static class Locations {
        /** Ячейка зоны приёмки: сюда поступает товар до размещения. */
        private int receivingId = 1;
        /** Ячейка зоны отгрузки: сюда перемещаются собранные заказы. */
        private int shippingId = 2;
    }
}
