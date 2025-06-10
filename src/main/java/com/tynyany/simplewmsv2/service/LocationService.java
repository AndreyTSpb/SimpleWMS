package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.entity.Location;
import com.tynyany.simplewmsv2.models.LocationTableString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {
    Location getLocationByID(int locationID);
    List<Location> getAllLocation();
    Page<LocationEntity> getAllLocationForTable(Pageable pageable);
    Page<LocationEntity> getAllLocationForTableWithFilter(String filter, Pageable pageable);
    void addLocation(Location location);
    void updateLocation(Location location);
    void delLocation(int locationID);
}
