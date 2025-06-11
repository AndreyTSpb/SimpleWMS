package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.*;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.entity.UserString;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.RoleRepository;
import com.tynyany.simplewmsv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService{
    final UserRepository userRepository;
    final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;


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
        UserEntity userEntity = new UserEntity(
                0,
                user.getLogin(),
                user.getPass(),
                user.getDel(),
                user.getEmployeeID()
        );
        userRepository.save(userEntity);
    }

    @Override
    public void updateUser(User user) {
        UserEntity userEntity = new UserEntity(
                user.getUserID(),
                user.getLogin(),
                user.getPass(),
                user.getDel(),
                user.getEmployeeID()
        );
        userRepository.save(userEntity);
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
}
