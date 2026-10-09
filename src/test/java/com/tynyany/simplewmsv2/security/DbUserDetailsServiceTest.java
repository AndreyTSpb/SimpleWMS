package com.tynyany.simplewmsv2.security;

import com.tynyany.simplewmsv2.dao.EmployeeEntity;
import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DbUserDetailsServiceTest {

    private UserRepository userRepository;
    private EmployeeRepository employeeRepository;
    private DbUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        service = new DbUserDetailsService(userRepository, employeeRepository);
    }

    @Test
    void roleOfEmployeeBecomesAuthority() {
        givenUser("ivanov", false, 3);
        givenEmployee(3, "Иванов И. И.", UserRole.STOREKEEPER.getRoleId(), 0);

        WmsUserDetails user = service.loadUserByUsername("ivanov");

        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_STOREKEEPER");
        assertThat(user.getEmployeeId()).isEqualTo(3);
        assertThat(user.getDisplayName()).isEqualTo("Иванов И. И.");
        assertThat(user.getRoleTitle()).isEqualTo("Кладовщик");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getPassword()).isEqualTo("HASH");
    }

    @Test
    void deletedUserOrEmployeeIsDisabled() {
        givenUser("old", true, 3);
        givenEmployee(3, "Иванов", UserRole.STOREKEEPER.getRoleId(), 0);
        assertThat(service.loadUserByUsername("old").isEnabled()).isFalse();

        givenUser("fired", false, 4);
        givenEmployee(4, "Смирнов", UserRole.STOREKEEPER.getRoleId(), 1);
        assertThat(service.loadUserByUsername("fired").isEnabled()).isFalse();
    }

    @Test
    void customRoleGivesNoAuthorities() {
        givenUser("guest", false, 5);
        givenEmployee(5, "Гость", 42, 0);

        WmsUserDetails user = service.loadUserByUsername("guest");

        assertThat(user.getAuthorities()).isEmpty();
        assertThat(user.getRoleTitle()).isEqualTo("Без роли");
    }

    @Test
    void unknownLoginIsRejected() {
        when(userRepository.findFirstByLoginIgnoreCase(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("nobody")).isInstanceOf(UsernameNotFoundException.class);
    }

    private void givenUser(String login, boolean del, int employeeId) {
        UserEntity user = new UserEntity(1, login, "HASH", del, employeeId);
        when(userRepository.findFirstByLoginIgnoreCase(login)).thenReturn(Optional.of(user));
    }

    private void givenEmployee(int id, String name, int roleId, int del) {
        EmployeeEntity e = new EmployeeEntity();
        e.setEmployeeID(id);
        e.setEmployeeName(name);
        e.setRoleID(roleId);
        e.setDel(del);
        when(employeeRepository.findById(id)).thenReturn(Optional.of(e));
    }
}
