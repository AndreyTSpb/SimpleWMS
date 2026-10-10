package com.tynyany.simplewmsv2.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Доступ к страницам и API, формат ответов API об ошибках — на полном контексте приложения.
 */
class ApiAndSecurityIT extends IntegrationTest {

    private static final String KEY = "test-api-key";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsService userDetailsService;

    /** Пользователь из демо-данных (V002), как после входа через форму. */
    private RequestPostProcessor login(String username) {
        return user(userDetailsService.loadUserByUsername(username));
    }

    // ---------- вход и права в интерфейсе ----------

    @Test
    void loginWithSeedAdministrator() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("admin"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/"));
        mvc.perform(formLogin("/login").user("admin").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/order_list")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void storekeeperSeesWarehouseOperationsButNotReportsAndOrders() throws Exception {
        var storekeeper = login("storekeeper");
        mvc.perform(get("/picking_route_list").with(storekeeper)).andExpect(status().isOk());
        mvc.perform(get("/shipping_list").with(storekeeper)).andExpect(status().isOk());
        mvc.perform(get("/warehouse_status").with(storekeeper)).andExpect(status().isForbidden());
        mvc.perform(get("/order_list").with(storekeeper)).andExpect(status().isForbidden());
        mvc.perform(get("/users").with(storekeeper)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/shipments").with(storekeeper)).andExpect(status().isForbidden());
    }

    @Test
    void managerOpensReportsAndApiDescriptionButNotAdministration() throws Exception {
        var manager = login("manager");
        mvc.perform(get("/warehouse_status").param("view", "cells").with(manager)).andExpect(status().isOk());
        mvc.perform(get("/movement_journal").with(manager)).andExpect(status().isOk());
        mvc.perform(get("/shipment_report").with(manager)).andExpect(status().isOk());
        mvc.perform(get("/erp_simulator").with(manager)).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs").with(manager))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/orders']").exists());
        mvc.perform(get("/users").with(manager)).andExpect(status().isForbidden());
    }

    // ---------- API: ключ, CSRF, формат ошибок ----------

    @Test
    void apiRequiresKey() throws Exception {
        mvc.perform(get("/api/v1/shipments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/v1/shipments").header("X-API-Key", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверный API-ключ"));
        mvc.perform(get("/api/v1/shipments").header("X-API-Key", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void browserSessionNeedsCsrfToken() throws Exception {
        String body = "[{\"code\": \"IT-S1\", \"name\": \"Поставщик из браузера\"}]";
        var manager = login("manager");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/suppliers")
                        .with(manager).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("CSRF")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/suppliers")
                        .with(manager).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(1));
    }

    @Test
    void validationErrorsAreReturnedPerFieldInRussian() throws Exception {
        mvc.perform(post("/api/v1/orders").header("X-API-Key", KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\": \"\", \"customerCode\": \"К000001\", \"lines\": [{\"code\": \"029952\", \"quantity\": -5}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("lines[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("должно быть больше 0")));
        mvc.perform(post("/api/v1/orders").header("X-API-Key", KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\": \"X\", \"customerCode\": \"К000001\", \"shipDate\": \"11.10.2026\", \"lines\": [{\"code\": \"029952\", \"quantity\": 1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("shipDate")));
        mvc.perform(post("/api/v1/orders").header("X-API-Key", KEY).contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Некорректный JSON")));
    }

    @Test
    void businessErrorsMapToNotFoundAndConflict() throws Exception {
        mvc.perform(get("/api/v1/orders/НЕТ-ТАКОГО").header("X-API-Key", KEY))
                .andExpect(status().isNotFound());
        String receiving = "{\"number\": \"IT-ДУБЛЬ\", \"supplierCode\": \"П000019\", \"date\": \"2026-10-10 08:00:00\","
                + " \"lines\": [{\"code\": \"029952\", \"quantity\": 5}]}";
        mvc.perform(post("/api/v1/receivings").header("X-API-Key", KEY).contentType(MediaType.APPLICATION_JSON).content(receiving))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/receivings").header("X-API-Key", KEY).contentType(MediaType.APPLICATION_JSON).content(receiving))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("уже загружена")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/products")
                        .header("X-API-Key", KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"code\": \"IT-X\", \"name\": \"Товар\", \"supplierCode\": \"НЕТ\"}]"))
                .andExpect(status().isNotFound());
    }
}
