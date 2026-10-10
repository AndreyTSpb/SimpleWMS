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
    private Api api = new Api();
    private Company company = new Company();
    private Placement placement = new Placement();
    private Expiry expiry = new Expiry();

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

    /**
     * Доступ внешних систем к REST API.
     */
    @Data
    public static class Api {
        /** Ключ для заголовка X-API-Key. Пустой — доступ по ключу отключён. */
        private String key = "";
    }

    /**
     * Реквизиты склада для печатных форм.
     */
    @Data
    public static class Company {
        /** Наименование грузополучателя в ТОРГ-12. */
        private String name = "Склад SimpleWMS";
    }

    /**
     * Правила подбора ячеек при размещении.
     */
    @Data
    public static class Placement {
        /**
         * Предпочтительная зона хранения (код зоны) для ABC-класса товара (код A/B/C/D),
         * например wms.placement.abc-zones.A=SC1. Без настройки подходят все зоны хранения.
         */
        private java.util.Map<String, String> abcZones = new java.util.HashMap<>();
    }

    /**
     * Контроль сроков годности в отчётах.
     */
    @Data
    public static class Expiry {
        /** За сколько дней до окончания срока партия подсвечивается как «истекает». */
        private int warningDays = 30;
    }
}
