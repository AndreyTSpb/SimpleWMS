package com.tynyany.simplewmsv2.security;

import com.tynyany.simplewmsv2.dao.EmployeeEntity;
import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Загрузка пользователя для входа из таблицы users.
 * Пользователь заблокирован, если он удалён (users.del) или удалён его сотрудник (employee.del).
 */
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public WmsUserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserEntity user = userRepository.findFirstByLoginIgnoreCase(login == null ? "" : login.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        EmployeeEntity employee = user.getEmployeeID() > 0
                ? employeeRepository.findById(user.getEmployeeID()).orElse(null)
                : null;

        UserRole role = employee != null ? UserRole.fromRoleId(employee.getRoleID()).orElse(null) : null;
        boolean enabled = !Boolean.TRUE.equals(user.getDel()) && (employee == null || employee.getDel() == 0);

        return new WmsUserDetails(
                user.getUserID(),
                user.getLogin(),
                user.getPass(),
                employee != null ? employee.getEmployeeID() : 0,
                employee != null ? employee.getEmployeeName() : "",
                role,
                enabled);
    }
}
