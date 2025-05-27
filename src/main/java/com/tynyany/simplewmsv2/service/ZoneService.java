package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Zone;

import java.util.List;

public interface ZoneService {
    Zone getByID(int zoneId);
    List<Zone> getAll();

    void add(Zone zone);

    void update(Zone zone);

    void del(Zone zone);
}
