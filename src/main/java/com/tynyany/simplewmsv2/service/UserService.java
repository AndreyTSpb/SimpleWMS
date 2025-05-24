package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.entity.UserString;

import java.util.List;


public interface  UserService {
    User getUserByID(int userId);// получить пользователя по ID
    List<User> getAllUser();

    List<UserString> getAllUserString();

    void addUser(User user);

    void updateUser(User user);

    void delUser(User user);
}
