package com.tynyany.simplewmsv2.security;

import com.tynyany.simplewmsv2.config.WmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Безопасность (см. Project_Docs/Архитектура.md, раздел «Безопасность»):
 * - интерфейс: вход по таблице users, права по роли сотрудника;
 * - REST API: заголовок X-API-Key (ERP) или сессия пользователя ADMIN/MANAGER (запросы со страниц).
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String ADMIN = UserRole.ADMIN.name();
    private static final String MANAGER = UserRole.MANAGER.name();
    private static final String STOREKEEPER = UserRole.STOREKEEPER.name();

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, WmsProperties properties) throws Exception {
        http.securityMatcher("/api/**")
                .addFilterBefore(new ApiKeyAuthenticationFilter(properties.getApi().getKey()),
                        UsernamePasswordAuthenticationFilter.class)
                // CSRF-токен обязателен для запросов из браузера с сессией пользователя;
                // ERP передаёт ключ и токен не использует, запрос без сессии подделать нечем
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        request -> request.getHeader(ApiKeyAuthenticationFilter.HEADER) != null
                                || request.getSession(false) == null))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasAnyRole("API", ADMIN, MANAGER))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .requestCache(cache -> cache.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/403", "/404").permitAll()
                        // администрирование
                        .requestMatchers("/users/**", "/roles/**", "/employees/**", "/options/**").hasRole(ADMIN)
                        // справочники, заказы и отчёты
                        .requestMatchers("/products/**", "/suppliers/**", "/customers/**", "/locations/**",
                                "/zones/**", "/order_list/**", "/warehouse_status/**", "/shipment_report/**",
                                "/income_report/**", "/report_pickers/**", "/product_report/**")
                        .hasAnyRole(ADMIN, MANAGER)
                        // складские операции: приёмка, размещение, перемещение, комплектация, отгрузка
                        .anyRequest().hasAnyRole(ADMIN, MANAGER, STOREKEEPER))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer configureWebSecurity() {
        return web -> web.ignoring().requestMatchers("/img/**", "/js/**", "/css/**", "/vendor/**", "/favicon.ico");
    }
}
