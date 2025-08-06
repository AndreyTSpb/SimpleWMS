package com.tynyany.simplewmsv2.entity_mappers;

import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.entity.PlacementRouteLine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlacementRouteLineToEntityMapper {
    PlacementRouteLineEntity placementRouteLineToEntityMapper(PlacementRouteLine placementRouteLine);
    PlacementRouteLine placementRouteLineToEntityMapper(PlacementRouteLineEntity placementRouteLineEntity);

}
