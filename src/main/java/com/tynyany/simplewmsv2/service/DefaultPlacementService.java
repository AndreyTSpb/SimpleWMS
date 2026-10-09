package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.domain.PlacementRouteStatus;
import com.tynyany.simplewmsv2.dto.UnplacedStockRow;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.PlacementRouteLineRepository;
import com.tynyany.simplewmsv2.repository.PlacementRouteRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import com.tynyany.simplewmsv2.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPlacementService implements PlacementService {

    private final StockRepository stockRepository;
    private final ReceivingLineRepository receivingLineRepository;
    private final PlacementRouteRepository placementRouteRepository;
    private final PlacementRouteLineRepository placementRouteLineRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final WmsProperties wmsProperties;

    @Override
    @Transactional
    public GenerationResult generateRoutes() {
        int receivingLocationId = wmsProperties.getLocations().getReceivingId();
        int routes = 0;
        int lines = 0;

        for (Integer zoneId : receivingLineRepository.findZonesWithUnplacedStock(receivingLocationId)) {
            if (zoneId == null) continue;
            List<UnplacedStockRow> rows = stockRepository.findUnplacedInZone(zoneId, receivingLocationId);
            if (rows.isEmpty()) continue;

            PlacementRouteEntity route = new PlacementRouteEntity();
            route.setDtCreate(new Timestamp(System.currentTimeMillis()));
            route.setStatus(PlacementRouteStatus.CREATED.getCode());
            route.setDel(false);
            route = placementRouteRepository.save(route);
            routes++;

            for (UnplacedStockRow row : rows) {
                placementRouteLineRepository.save(new PlacementRouteLineEntity(
                        0,
                        route.getPRouteId(),
                        row.getQuantity(),
                        row.getTargetLocationId(),
                        row.getBatchId(),
                        row.getProductId(),
                        false,
                        false
                ));
                receivingLineRepository.findById(row.getReceivingLineId()).ifPresent(line -> {
                    line.setPlacementRoute(true);
                    receivingLineRepository.save(line);
                });
                lines++;
            }
            log.info("Маршрут размещения id={} для зоны {}: строк {}", route.getPRouteId(), zoneId, rows.size());
        }
        return new GenerationResult(routes, lines);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteView> getRoutes() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        List<RouteView> result = new ArrayList<>();
        for (PlacementRouteEntity route : placementRouteRepository.findActiveOrderByIdDesc()) {
            List<RouteLineView> lines = new ArrayList<>();
            int total = 0;
            String zone = "";
            for (PlacementRouteLineEntity line : placementRouteLineRepository.findByRouteId(route.getPRouteId())) {
                ProductEntity product = productRepository.findById(line.getProductId()).orElse(null);
                LocationEntity location = locationRepository.findById(line.getLocationId()).orElse(null);
                if (zone.isEmpty() && location != null) {
                    zone = zoneRepository.findById(location.getZoneID())
                            .map(z -> z.getCode() + " — " + z.getZoneName())
                            .orElse("");
                }
                lines.add(new RouteLineView(
                        product != null ? product.getProductCode() : "?",
                        product != null ? product.getProductName() : "товар " + line.getProductId(),
                        line.getBatchId(),
                        location != null ? location.getLocationCode() : "ячейка " + line.getLocationId(),
                        line.getQnt(),
                        Boolean.TRUE.equals(line.getComplete())));
                total += line.getQnt();
            }
            result.add(new RouteView(
                    route.getPRouteId(),
                    route.getDtCreate() != null ? format.format(route.getDtCreate()) : "",
                    zone,
                    PlacementRouteStatus.fromCode(route.getStatus()).getTitle(),
                    total,
                    lines));
        }
        return result;
    }
}
