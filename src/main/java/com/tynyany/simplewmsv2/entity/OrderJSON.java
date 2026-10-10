package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Заказ клиента из ERP (POST /api/new_order).
 * <pre>
 * {"header": {"orderERP": "ПР004139814", "customerCode": "К000001",
 *             "orderDate": "2026-10-10 09:00:00", "shipDate": "2026-10-11", "note": "..."},
 *  "body": [{"code": "029952", "qnt": "10"}, ...]}
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderJSON {
    public Header header;
    public List<Line> body = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        public String orderERP;
        public String customerCode;
        public String orderDate;
        /** Плановая дата отгрузки, формат ГГГГ-ММ-ДД. */
        public String shipDate;
        public String note;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Line {
        public String code;
        public String qnt;
    }
}
