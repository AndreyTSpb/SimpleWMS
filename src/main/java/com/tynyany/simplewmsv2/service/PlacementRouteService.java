package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.PlacementRoute;
import com.tynyany.simplewmsv2.entity.Zone;

import java.util.List;

public interface PlacementRouteService {

    PlacementRoute getByID(int id);

    List<PlacementRoute> getAll();

    int add(PlacementRoute placementRoute);

    void update(PlacementRoute placementRoute);

    void del(int id);
}
