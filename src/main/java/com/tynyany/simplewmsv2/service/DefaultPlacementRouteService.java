package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import com.tynyany.simplewmsv2.entity.PlacementRoute;
import com.tynyany.simplewmsv2.entity_mappers.PlacementRouteToEntityMapper;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.repository.PlacementRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DefaultPlacementRouteService implements PlacementRouteService{

    final PlacementRouteRepository placementRouteRepository;
    final PlacementRouteToEntityMapper placementRouteToEntityMapper;

    @Override
    public PlacementRoute getByID(int id) {
        PlacementRouteEntity placementRouteEntity = placementRouteRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Placement Route not found:id =" + id));


        return placementRouteToEntityMapper.placementRouterToEntityMapper(placementRouteEntity);
    }

    @Override
    public List<PlacementRoute> getAll() {
        return null;
    }

    @Override
    public int add(PlacementRoute placementRoute) {
        PlacementRouteEntity entity = placementRouteToEntityMapper.placementRouterToEntityMapper(placementRoute);
        PlacementRouteEntity entity1 = placementRouteRepository.save(entity);
        return entity1.getPRouteId();

    }

    @Override
    public void update(PlacementRoute placementRoute) {
        PlacementRouteEntity entity = placementRouteToEntityMapper.placementRouterToEntityMapper(placementRoute);
        placementRouteRepository.save(entity);
    }

    @Override
    public void del(int id) {
        PlacementRoute obj = getByID(id);
        obj.setDel(true);
        placementRouteRepository.save(placementRouteToEntityMapper.placementRouterToEntityMapper(obj));
    }
}
