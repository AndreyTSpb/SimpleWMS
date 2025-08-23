package com.tynyany.simplewmsv2.entity_mappers;

import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import com.tynyany.simplewmsv2.entity.PlacementRoute;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlacementRouteToEntityMapper {
    PlacementRouteEntity placementRouterToEntityMapper(PlacementRoute placementRoute);
    PlacementRoute placementRouterToEntityMapper(PlacementRouteEntity placementRouteEntity);
}
