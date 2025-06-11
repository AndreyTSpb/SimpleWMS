package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.UserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

}
