package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    Page<UserEntity> findAll(Pageable pageable);
    Page<UserEntity> findByLoginLikeIgnoreCase(Pageable pageable, String login);
}
