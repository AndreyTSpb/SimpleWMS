package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.dao.UserRepository;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService{
    final UserRepository userRepository;


    @Override
    public User getUserByID(int userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found:id =" +userId));
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
