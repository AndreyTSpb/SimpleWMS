package com.tynyany.simplewmsv2.dao;

import com.tynyany.simplewmsv2.entity.Zone;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ZoneRepository extends CrudRepository<ZoneEntity, Integer> {
    Optional<ZoneEntity> findByZoneName(String zoneName);
}
