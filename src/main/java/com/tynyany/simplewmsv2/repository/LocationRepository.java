package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;


import java.util.Optional;

public interface LocationRepository extends CrudRepository<LocationEntity, Integer> {
    Optional<LocationEntity> findByLocationCode(String name);
    Page<LocationEntity> findAllBy(Pageable pageable);
    Page<LocationEntity> findByLocationCodeIsLikeIgnoreCase(String filter, Pageable pageable);
}
