package com.tynyany.simplewmsv2.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Доступ к пользователю, выполняющему текущий запрос.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<WmsUserDetails> get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof WmsUserDetails user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /** id сотрудника текущего пользователя или 0 (запрос по API-ключу, сотрудник не привязан). */
    public static int employeeId() {
        return get().map(WmsUserDetails::getEmployeeId).orElse(0);
    }
}
