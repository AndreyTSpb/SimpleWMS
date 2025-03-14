package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Location;

import java.util.List;

public interface LocationService {
    Location getLocationByID(int locationID);
    List<Location> getAllLocation();
    void addLocation(Location location);
    void updateLocation(Location location);
    void delLocation(int locationID);
}
