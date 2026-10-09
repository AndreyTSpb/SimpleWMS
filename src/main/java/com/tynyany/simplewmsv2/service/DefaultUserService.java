package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.*;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.entity.UserString;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.RoleRepository;
import com.tynyany.simplewmsv2.repository.UserRepository;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService{
    final UserRepository userRepository;
    final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public User getUserByID(int userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found:id =" + userId));
        return new User(
                userEntity.getUserID(),
                userEntity.getLogin(),
                userEntity.getPass(),
                userEntity.getDel(),
                userEntity.getEmployeeID()
                );
    }

    @Override
    public List<User> getAllUser() {
        Iterable<UserEntity> iterable = userRepository.findAll();
        ArrayList<User> users = new ArrayList<>();
        for(UserEntity userEntity : iterable){
            users.add(new User(
                    userEntity.getUserID(),
                    userEntity.getLogin(),
                    userEntity.getPass(),
                    userEntity.getDel(),
                    userEntity.getEmployeeID()
                    )
            );
        }
        return users;
    }

    //
    public List<UserString> getAllUserString() {
        Iterable<UserEntity> iterable = userRepository.findAll();
        ArrayList<UserString> users = new ArrayList<>();
        for(UserEntity userEntity : iterable){

            //через айди получаем фио сотрудника привязанного
           int employeeID = userEntity.getEmployeeID();
           int roleId = 0;
           String employeeName = "";
           String roleName = "";
           if(employeeID != 0){
               EmployeeEntity employee = employeeRepository.findById(employeeID)
                       .orElseThrow(() -> new UserNotFoundException("Employee not found:id = " + employeeID));
               employeeName = employee.getEmployeeName();
               if(employee.getRoleID() != 0){
                   roleId = employee.getRoleID();
                   RoleEntity roleEntity = roleRepository.findById(roleId)
                           .orElseThrow(() -> new UserNotFoundException("Role not found:id = " + employee.getRoleID()));
                   roleName = roleEntity.getRoleName();
               }
           }

            users.add(new UserString(
                    userEntity.getUserID(),
                    userEntity.getLogin(),
                    userEntity.getDel(),
                    employeeID,
                    employeeName,
                    roleId,
                    roleName
            ));
        }
        return users;
    }

    @Override
    public void addUser(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new WmsException("Не указан логин");
        }
        if (userRepository.findFirstByLoginIgnoreCase(user.getLogin().trim()).isPresent()) {
            throw new WmsException("Пользователь с логином " + user.getLogin() + " уже существует");
        }
        checkPassword(user.getPass());
        UserEntity userEntity = new UserEntity(
                0,
                user.getLogin().trim(),
                passwordEncoder.encode(user.getPass()),
                user.getDel(),
                user.getEmployeeID()
        );
        userRepository.save(userEntity);
    }

    @Override
    public void updateUser(User user) {
        // Форма редактирования не передаёт пароль: сохраняем текущий хеш
        String passwordHash = userRepository.findById(user.getUserID())
                .map(UserEntity::getPass)
                .orElse(null);
        UserEntity userEntity = new UserEntity(
                user.getUserID(),
                user.getLogin(),
                passwordHash,
                user.getDel(),
                user.getEmployeeID()
        );
        userRepository.save(userEntity);
    }

    @Override
    public void changePassword(int userId, String newPassword, String confirmation) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь " + userId + " не найден"));
        checkPassword(newPassword);
        if (!newPassword.equals(confirmation)) {
            throw new WmsException("Пароли не совпадают");
        }
        user.setPass(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private static void checkPassword(String password) {
        if (password == null || password.length() < 4) {
            throw new WmsException("Пароль должен содержать не менее 4 символов");
        }
    }

    public void delUser(User user) {
        UserEntity userEntity = new UserEntity(
                user.getUserID(),
                user.getLogin(),
                user.getPass(),
                true,
                user.getEmployeeID()
        );
        userRepository.save(userEntity);
    }

    @Override
    public Page<UserEntity> getAllUserPageable(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<UserEntity> getAllUserPageableWithFilter(Pageable pageable, String filter) {
        return userRepository.findByLoginLikeIgnoreCase(pageable, filter);
    }
}
