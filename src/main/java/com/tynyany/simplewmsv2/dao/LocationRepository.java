package com.tynyany.simplewmsv2.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LocationRepository extends CrudRepository<LocationEntity, Integer> {
    Optional<LocationEntity> findByLocationCode(String name);
}
