package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.entity.UserString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface  UserService {
    User getUserByID(int userId);// получить пользователя по ID
    List<User> getAllUser();

    List<UserString> getAllUserString();

    void addUser(User user);

    void updateUser(User user);

    void delUser(User user);
    Page<UserEntity> getAllUserPageable(Pageable pageable);
    Page<UserEntity> getAllUserPageableWithFilter(Pageable pageable, String filter);
}
