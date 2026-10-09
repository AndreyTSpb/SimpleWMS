package com.tynyany.simplewmsv2.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Вошедший пользователь: учётная запись из users + сотрудник и его роль.
 */
@Getter
public class WmsUserDetails implements UserDetails {

    private final int userId;
    private final String login;
    private final String passwordHash;
    private final int employeeId;
    private final String employeeName;
    /** null — роль сотрудника не относится к системным, доступа к разделам нет. */
    private final UserRole role;
    private final boolean enabled;

    public WmsUserDetails(int userId, String login, String passwordHash, int employeeId,
                          String employeeName, UserRole role, boolean enabled) {
        this.userId = userId;
        this.login = login;
        this.passwordHash = passwordHash;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.enabled = enabled;
    }

    /** Имя для отображения в интерфейсе. */
    public String getDisplayName() {
        return employeeName != null && !employeeName.isBlank() ? employeeName : login;
    }

    public String getRoleTitle() {
        return role != null ? role.getTitle() : "Без роли";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role == null ? List.of() : List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
