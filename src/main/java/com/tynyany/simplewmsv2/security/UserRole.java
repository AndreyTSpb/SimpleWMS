package com.tynyany.simplewmsv2.security;

import lombok.Getter;

import java.util.Optional;

/**
 * Роли доступа. Связаны с системными записями таблицы roles (создаются скриптом V001):
 * роль сотрудника (employee.roleid) определяет права пользователя.
 */
@Getter
public enum UserRole {
    ADMIN(1, "Администратор"),
    MANAGER(2, "Менеджер склада"),
    STOREKEEPER(3, "Кладовщик");

    private final int roleId;
    private final String title;

    UserRole(int roleId, String title) {
        this.roleId = roleId;
        this.title = title;
    }

    /** Имя полномочия Spring Security, например ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }

    public static Optional<UserRole> fromRoleId(int roleId) {
        for (UserRole role : values()) {
            if (role.roleId == roleId) return Optional.of(role);
        }
        return Optional.empty();
    }
}
