package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.ABCEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ZoneEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.PlacementRouteStatus;
import com.tynyany.simplewmsv2.dto.LocationVolume;
import com.tynyany.simplewmsv2.dto.UnplacedStockRow;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.ABCRepository;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.PlacementRouteLineRepository;
import com.tynyany.simplewmsv2.repository.PlacementRouteRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import com.tynyany.simplewmsv2.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private final ABCRepository abcRepository;
    private final BatchRepository batchRepository;
    private final EmployeeRepository employeeRepository;
    private final InventoryService inventoryService;
    private final WmsProperties wmsProperties;

    /** Строка будущего маршрута. */
    private record PlannedLine(UnplacedStockRow row, PlacementCellPicker.Cell cell) {}

    @Override
    @Transactional
    public GenerationResult generateRoutes() {
        int receivingLocationId = wmsProperties.getLocations().getReceivingId();
        List<UnplacedStockRow> rows = stockRepository.findUnplaced(receivingLocationId);
        if (rows.isEmpty()) {
            return new GenerationResult(0, 0, 0);
        }

        PlacementCellPicker picker = new PlacementCellPicker(
                storageCells(), occupiedVolume(), wmsProperties.getPlacement().getAbcZones());
        Map<Integer, String> abcCodes = new HashMap<>();
        for (ABCEntity abc : abcRepository.findAll()) {
            abcCodes.put(abc.getAbcID(), abc.getCode());
        }

        // Распределяем строки по ячейкам и группируем по зонам хранения
        Map<Integer, List<PlannedLine>> byZone = new LinkedHashMap<>();
        int skipped = 0;
        for (UnplacedStockRow row : rows) {
            ProductEntity product = productRepository.findById(row.getProductId()).orElse(null);
            double volume = row.getQuantity() * (product != null && product.getVolume() != null ? product.getVolume() : 0.0);

            Optional<PlacementCellPicker.Cell> cell = row.getTargetLocationId() > 0
                    ? picker.find(row.getTargetLocationId())
                    : Optional.empty();
            if (cell.isPresent()) {
                picker.reserve(cell.get().id(), volume);
            } else {
                String abcCode = product != null ? abcCodes.get(product.getAbcID()) : null;
                Set<Integer> withProduct = new HashSet<>(stockRepository.findLocationsWithProduct(row.getProductId()));
                cell = picker.pick(abcCode, volume, withProduct);
            }
            if (cell.isEmpty()) {
                skipped++;
                log.warn("Нет свободной ячейки для партии {} (товар {}, объём {} л)", row.getBatchId(), row.getProductId(), volume);
                continue;
            }
            byZone.computeIfAbsent(cell.get().zoneId(), z -> new ArrayList<>()).add(new PlannedLine(row, cell.get()));
        }

        int lines = 0;
        for (Map.Entry<Integer, List<PlannedLine>> zone : byZone.entrySet()) {
            PlacementRouteEntity route = new PlacementRouteEntity();
            route.setDtCreate(now());
            route.setStatus(PlacementRouteStatus.CREATED.getCode());
            route.setDel(false);
            route = placementRouteRepository.save(route);

            for (PlannedLine planned : zone.getValue()) {
                UnplacedStockRow row = planned.row();
                placementRouteLineRepository.save(new PlacementRouteLineEntity(
                        0, route.getPRouteId(), row.getQuantity(), planned.cell().id(),
                        row.getBatchId(), row.getProductId(), false, false));
                markReceivingLine(row.getReceivingLineId(), true);
                lines++;
            }
            log.info("Маршрут размещения id={} для зоны {}: строк {}", route.getPRouteId(), zone.getKey(), zone.getValue().size());
        }
        return new GenerationResult(byZone.size(), lines, skipped);
    }

    @Override
    @Transactional
    public void startRoute(int routeId, int employeeId) {
        PlacementRouteEntity route = getRouteEntity(routeId);
        requireStatus(route, PlacementRouteStatus.CREATED, "Взять маршрут в работу");
        route.setStatus(PlacementRouteStatus.IN_PROGRESS.getCode());
        route.setEmployerId(employeeId);
        route.setDtStart(now());
        placementRouteRepository.save(route);
        log.info("Маршрут размещения id={} взят в работу, сотрудник {}", routeId, employeeId);
    }

    @Override
    @Transactional
    public boolean confirmLine(int routeLineId, int actualLocationId, int employeeId) {
        PlacementRouteLineEntity line = placementRouteLineRepository.findById(routeLineId)
                .orElseThrow(() -> new NotFoundException("Строка маршрута " + routeLineId + " не найдена"));
        PlacementRouteEntity route = getRouteEntity(line.getPRouteId());
        requireStatus(route, PlacementRouteStatus.IN_PROGRESS, "Подтвердить размещение");
        if (Boolean.TRUE.equals(line.getComplete())) {
            throw new WmsException("Строка маршрута " + routeLineId + " уже подтверждена");
        }

        int targetId = actualLocationId > 0 ? actualLocationId : line.getLocationId();
        LocationEntity target = locationRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Ячейка " + targetId + " не найдена"));
        if (target.getDel() != 0 || isSystemLocation(targetId) || isSystemZone(target.getZoneID())) {
            throw new WmsException("Ячейка " + target.getLocationCode() + " не является местом хранения");
        }

        inventoryService.move(line.getBatchId(), line.getProductId(),
                wmsProperties.getLocations().getReceivingId(), targetId,
                line.getQnt(), employeeId, MovementType.PLACEMENT);

        line.setLocationId(targetId);
        line.setComplete(true);
        placementRouteLineRepository.save(line);

        boolean allDone = placementRouteLineRepository.findByRouteId(route.getPRouteId()).stream()
                .allMatch(l -> Boolean.TRUE.equals(l.getComplete()));
        if (allDone) {
            route.setStatus(PlacementRouteStatus.COMPLETED.getCode());
            route.setDtStop(now());
            placementRouteRepository.save(route);
            log.info("Маршрут размещения id={} завершён", route.getPRouteId());
        }
        return allDone;
    }

    @Override
    @Transactional
    public void cancelRoute(int routeId) {
        PlacementRouteEntity route = getRouteEntity(routeId);
        PlacementRouteStatus status = PlacementRouteStatus.fromCode(route.getStatus());
        if (status != PlacementRouteStatus.CREATED && status != PlacementRouteStatus.IN_PROGRESS) {
            throw new WmsException("Отменить можно только маршрут в статусе «Создан» или «В работе»");
        }
        List<PlacementRouteLineEntity> lines = placementRouteLineRepository.findByRouteId(routeId);
        if (lines.stream().anyMatch(l -> Boolean.TRUE.equals(l.getComplete()))) {
            throw new WmsException("Нельзя отменить маршрут: часть строк уже размещена");
        }
        for (PlacementRouteLineEntity line : lines) {
            batchRepository.findById(line.getBatchId())
                    .ifPresent(batch -> markReceivingLine(batch.getReceiving_id(), false));
        }
        route.setStatus(PlacementRouteStatus.CANCELLED.getCode());
        placementRouteRepository.save(route);
        log.info("Маршрут размещения id={} отменён", routeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteView> getRoutes() {
        List<RouteView> result = new ArrayList<>();
        for (PlacementRouteEntity route : placementRouteRepository.findActiveOrderByIdDesc()) {
            result.add(toView(route));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RouteView getRoute(int routeId) {
        return toView(getRouteEntity(routeId));
    }

    // ---------- вспомогательные ----------

    private RouteView toView(PlacementRouteEntity route) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        List<RouteLineView> lines = new ArrayList<>();
        int total = 0;
        int completed = 0;
        String zone = "";
        for (PlacementRouteLineEntity line : placementRouteLineRepository.findByRouteId(route.getPRouteId())) {
            ProductEntity product = productRepository.findById(line.getProductId()).orElse(null);
            LocationEntity location = locationRepository.findById(line.getLocationId()).orElse(null);
            if (zone.isEmpty() && location != null) {
                zone = zoneRepository.findById(location.getZoneID())
                        .map(z -> z.getCode() + " — " + z.getZoneName())
                        .orElse("");
            }
            boolean complete = Boolean.TRUE.equals(line.getComplete());
            lines.add(new RouteLineView(
                    line.getPRouteLineId(),
                    product != null ? product.getProductCode() : "?",
                    product != null ? product.getProductName() : "товар " + line.getProductId(),
                    line.getBatchId(),
                    line.getLocationId(),
                    location != null ? location.getLocationCode() : "ячейка " + line.getLocationId(),
                    line.getQnt(),
                    complete));
            total += line.getQnt();
            if (complete) completed++;
        }
        lines.sort((a, b) -> a.locationCode().compareTo(b.locationCode()));
        String employee = route.getEmployerId() > 0
                ? employeeRepository.findById(route.getEmployerId()).map(e -> e.getEmployeeName()).orElse("")
                : "";
        PlacementRouteStatus status = PlacementRouteStatus.fromCode(route.getStatus());
        return new RouteView(
                route.getPRouteId(),
                route.getDtCreate() != null ? format.format(route.getDtCreate()) : "",
                zone,
                status.getCode(),
                status.getTitle(),
                employee,
                total,
                completed,
                lines);
    }

    /** Активные ячейки хранения: не удалённые, не системные и не в зонах системных ячеек. */
    private List<PlacementCellPicker.Cell> storageCells() {
        Map<Integer, String> zoneCodes = new HashMap<>();
        for (ZoneEntity zone : zoneRepository.findAll()) {
            if (!Boolean.TRUE.equals(zone.getDel())) zoneCodes.put(zone.getZoneId(), zone.getCode());
        }
        List<PlacementCellPicker.Cell> cells = new ArrayList<>();
        for (LocationEntity l : locationRepository.findAll()) {
            if (l.getDel() != 0 || isSystemLocation(l.getLocationID()) || isSystemZone(l.getZoneID())
                    || !zoneCodes.containsKey(l.getZoneID())) {
                continue;
            }
            cells.add(new PlacementCellPicker.Cell(l.getLocationID(), l.getLocationCode(), l.getZoneID(),
                    zoneCodes.get(l.getZoneID()), l.getRow(), l.getX(), l.getY(), l.getZ(), l.getCapacity()));
        }
        return cells;
    }

    /** Занятый объём ячеек: остатки + открытые маршруты размещения. */
    private Map<Integer, Double> occupiedVolume() {
        Map<Integer, Double> used = new HashMap<>();
        for (LocationVolume v : stockRepository.sumVolumeByLocation()) {
            used.merge(v.getLocationId(), v.getVolume() == null ? 0.0 : v.getVolume(), Double::sum);
        }
        for (LocationVolume v : placementRouteLineRepository.sumPlannedVolumeByLocation()) {
            used.merge(v.getLocationId(), v.getVolume() == null ? 0.0 : v.getVolume(), Double::sum);
        }
        return used;
    }

    private boolean isSystemLocation(int locationId) {
        WmsProperties.Locations system = wmsProperties.getLocations();
        return locationId == system.getReceivingId() || locationId == system.getShippingId();
    }

    /** Зона системной ячейки (приёмки или отгрузки) — не зона хранения. */
    private boolean isSystemZone(int zoneId) {
        WmsProperties.Locations system = wmsProperties.getLocations();
        for (int id : new int[]{system.getReceivingId(), system.getShippingId()}) {
            Optional<LocationEntity> l = locationRepository.findById(id);
            if (l.isPresent() && l.get().getZoneID() == zoneId) return true;
        }
        return false;
    }

    private void markReceivingLine(int receivingLineId, boolean inRoute) {
        receivingLineRepository.findById(receivingLineId).ifPresent(line -> {
            line.setPlacementRoute(inRoute);
            receivingLineRepository.save(line);
        });
    }

    private PlacementRouteEntity getRouteEntity(int routeId) {
        return placementRouteRepository.findById(routeId)
                .filter(r -> !Boolean.TRUE.equals(r.getDel()))
                .orElseThrow(() -> new NotFoundException("Маршрут размещения " + routeId + " не найден"));
    }

    private void requireStatus(PlacementRouteEntity route, PlacementRouteStatus expected, String action) {
        PlacementRouteStatus actual = PlacementRouteStatus.fromCode(route.getStatus());
        if (actual != expected) {
            throw new WmsException(action + " можно только для маршрута в статусе «" + expected.getTitle()
                    + "», текущий статус: «" + actual.getTitle() + "»");
        }
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
