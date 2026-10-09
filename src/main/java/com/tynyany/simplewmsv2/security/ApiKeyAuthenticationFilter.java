package com.tynyany.simplewmsv2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Аутентификация внешней системы (ERP) по заголовку X-API-Key.
 * Запрос с верным ключом получает роль API; с неверным — отклоняется с кодом 401.
 * Запросы без заголовка проходят дальше (например, из браузера с сессией пользователя).
 */
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";
    public static final String ROLE_API = "ROLE_API";

    private final String expectedKey;

    public ApiKeyAuthenticationFilter(String expectedKey) {
        this.expectedKey = expectedKey == null ? "" : expectedKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        if (key == null) {
            chain.doFilter(request, response);
            return;
        }
        if (expectedKey.isBlank() || !MessageDigest.isEqual(
                key.getBytes(StandardCharsets.UTF_8), expectedKey.getBytes(StandardCharsets.UTF_8))) {
            log.warn("API: неверный ключ доступа, {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":0,\"message\":\"Неверный API-ключ\"}");
            return;
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "erp", null, List.of(new SimpleGrantedAuthority(ROLE_API))));
        SecurityContextHolder.setContext(context);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
