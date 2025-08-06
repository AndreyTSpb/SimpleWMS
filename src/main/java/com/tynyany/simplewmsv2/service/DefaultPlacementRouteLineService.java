package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.entity.PlacementRoute;
import com.tynyany.simplewmsv2.entity.PlacementRouteLine;
import com.tynyany.simplewmsv2.entity_mappers.PlacementRouteLineToEntityMapper;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.repository.PlacementRouteLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DefaultPlacementRouteLineService implements PlacementRouteLineService{

    final PlacementRouteLineRepository placementRouteLineRepository;
    final PlacementRouteLineToEntityMapper placementRouteLineToEntityMapper;

    @Override
    public PlacementRouteLine getByID(int id) {
        PlacementRouteLineEntity entity = placementRouteLineRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Placement Route line not found:id =" + id));
        return placementRouteLineToEntityMapper.placementRouteLineToEntityMapper(entity);
    }

    @Override
    public List<PlacementRouteLine> getAll() {
        return null;
    }

    @Override
    public int add(PlacementRouteLine placementRouteLine) {
        PlacementRouteLineEntity entity = placementRouteLineToEntityMapper.placementRouteLineToEntityMapper(placementRouteLine);
        PlacementRouteLineEntity entity1 = placementRouteLineRepository.save(entity);
        return entity1.getPRouteId();
    }

    @Override
    public void update(PlacementRouteLine placementRouteLine) {
        placementRouteLineRepository.save(placementRouteLineToEntityMapper.placementRouteLineToEntityMapper(placementRouteLine));
    }

    @Override
    public void del(int id) {

    }
}
