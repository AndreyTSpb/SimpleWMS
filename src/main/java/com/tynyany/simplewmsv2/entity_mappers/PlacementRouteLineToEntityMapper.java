package com.tynyany.simplewmsv2.entity_mappers;

import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.entity.PlacementRouteLine;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlacementRouteLineToEntityMapper {
    PlacementRouteLineEntity placementRouteLineToEntityMapper(PlacementRouteLine placementRouteLine);
    PlacementRouteLine placementRouteLineToEntityMapper(PlacementRouteLineEntity placementRouteLineEntity);

}
