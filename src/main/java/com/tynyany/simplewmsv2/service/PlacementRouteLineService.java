package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.PlacementRoute;
import com.tynyany.simplewmsv2.entity.PlacementRouteLine;

import java.util.List;

public interface PlacementRouteLineService {
    PlacementRouteLine getByID(int id);

    List<PlacementRouteLine> getAll();

    int add(PlacementRouteLine placementRouteLine);

    void update(PlacementRouteLine placementRouteLine);

    void del(int id);
}
