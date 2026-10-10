package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.ABCEntity;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.dao.ZoneEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.PlacementRouteStatus;
import com.tynyany.simplewmsv2.dto.UnplacedStockRow;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultPlacementServiceTest {

    private static final int RECEIVING_CELL = 1;

    private StockRepository stockRepository;
    private ReceivingLineRepository lineRepository;
    private PlacementRouteRepository routeRepository;
    private PlacementRouteLineRepository routeLineRepository;
    private ProductRepository productRepository;
    private LocationRepository locationRepository;
    private BatchRepository batchRepository;
    private InventoryService inventoryService;
    private DefaultPlacementService service;
    private final List<PlacementRouteLineEntity> savedLines = new ArrayList<>();

    @BeforeEach
    void setUp() {
        stockRepository = mock(StockRepository.class);
        lineRepository = mock(ReceivingLineRepository.class);
        routeRepository = mock(PlacementRouteRepository.class);
        routeLineRepository = mock(PlacementRouteLineRepository.class);
        productRepository = mock(ProductRepository.class);
        locationRepository = mock(LocationRepository.class);
        ZoneRepository zoneRepository = mock(ZoneRepository.class);
        ABCRepository abcRepository = mock(ABCRepository.class);
        batchRepository = mock(BatchRepository.class);
        inventoryService = mock(InventoryService.class);

        WmsProperties properties = new WmsProperties();
        properties.getLocations().setReceivingId(RECEIVING_CELL);
        properties.getLocations().setShippingId(2);
        properties.getPlacement().setAbcZones(Map.of("A", "SC1", "C", "SC2"));

        service = new DefaultPlacementService(stockRepository, lineRepository, routeRepository, routeLineRepository,
                productRepository, locationRepository, zoneRepository, abcRepository, batchRepository,
                mock(EmployeeRepository.class), inventoryService, properties);

        // зоны: 1 — приёмка, 3 — SC1, 4 — SC2; ячейки по 100 л
        when(zoneRepository.findAll()).thenReturn(List.of(zone(1, "PRM"), zone(3, "SC1"), zone(4, "SC2")));
        List<LocationEntity> cells = List.of(cell(1, "PRM-01-01-1-1", 1, 1000), cell(101, "SC1-01-01-1-1", 3, 100),
                cell(102, "SC1-01-02-1-1", 3, 100), cell(201, "SC2-01-01-1-1", 4, 100));
        when(locationRepository.findAll()).thenReturn(cells);
        for (LocationEntity c : cells) {
            when(locationRepository.findById(c.getLocationID())).thenReturn(Optional.of(c));
        }
        when(abcRepository.findAll()).thenReturn(List.of(new ABCEntity(1, "A", "", false), new ABCEntity(3, "C", "", false)));
        when(productRepository.findById(5)).thenReturn(Optional.of(product(5, 1, 0.5f)));    // класс A, 0,5 л
        when(productRepository.findById(6)).thenReturn(Optional.of(product(6, 3, 10f)));     // класс C, 10 л

        when(routeRepository.save(any())).thenAnswer(inv -> {
            PlacementRouteEntity r = inv.getArgument(0);
            if (r.getPRouteId() == 0) r.setPRouteId(50 + savedRoutes++);
            return r;
        });
        when(routeLineRepository.save(any())).thenAnswer(inv -> {
            savedLines.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
    }

    private int savedRoutes = 0;

    @Test
    void generateUsesChosenCellOrPicksByAbcAndGroupsByZone() {
        ReceivingLineEntity rl1 = receivingLine(11);
        ReceivingLineEntity rl2 = receivingLine(12);
        when(stockRepository.findUnplaced(RECEIVING_CELL)).thenReturn(List.of(
                row(77, 5, 100, 102, 11),     // ячейка выбрана при приёмке
                row(78, 6, 5, 0, 12)));       // подобрать автоматически: класс C → SC2
        when(lineRepository.findById(11)).thenReturn(Optional.of(rl1));
        when(lineRepository.findById(12)).thenReturn(Optional.of(rl2));

        PlacementService.GenerationResult result = service.generateRoutes();

        assertThat(result.routesCreated()).isEqualTo(2);
        assertThat(result.linesCreated()).isEqualTo(2);
        assertThat(result.linesSkipped()).isZero();
        assertThat(savedLines).extracting(PlacementRouteLineEntity::getLocationId).containsExactly(102, 201);
        assertThat(savedLines).extracting(PlacementRouteLineEntity::getQnt).containsExactly(100, 5);
        assertThat(rl1.getPlacementRoute()).isTrue();
        assertThat(rl2.getPlacementRoute()).isTrue();
    }

    @Test
    void generateSkipsRowsWithoutSpace() {
        when(stockRepository.findUnplaced(RECEIVING_CELL)).thenReturn(List.of(row(78, 6, 50, 0, 12)));  // 500 л
        PlacementService.GenerationResult result = service.generateRoutes();
        assertThat(result.routesCreated()).isZero();
        assertThat(result.linesSkipped()).isEqualTo(1);
        verify(routeRepository, never()).save(any());
    }

    @Test
    void confirmMovesStockAndCompletesRoute() {
        PlacementRouteEntity route = route(50, PlacementRouteStatus.IN_PROGRESS);
        PlacementRouteLineEntity line = new PlacementRouteLineEntity(7, 50, 100, 101, 77, 5, false, false);
        when(routeLineRepository.findById(7)).thenReturn(Optional.of(line));
        when(routeLineRepository.findByRouteId(50)).thenReturn(List.of(line));

        boolean done = service.confirmLine(7, 0, 3);

        verify(inventoryService).move(77, 5, RECEIVING_CELL, 101, 100, 3, MovementType.PLACEMENT);
        assertThat(line.getComplete()).isTrue();
        assertThat(done).isTrue();
        assertThat(route.getStatus()).isEqualTo(PlacementRouteStatus.COMPLETED.getCode());
        assertThat(route.getDtStop()).isNotNull();
    }

    @Test
    void confirmToAnotherCellRecordsActualCell() {
        route(50, PlacementRouteStatus.IN_PROGRESS);
        PlacementRouteLineEntity line = new PlacementRouteLineEntity(7, 50, 100, 101, 77, 5, false, false);
        PlacementRouteLineEntity other = new PlacementRouteLineEntity(8, 50, 1, 101, 79, 5, false, false);
        when(routeLineRepository.findById(7)).thenReturn(Optional.of(line));
        when(routeLineRepository.findByRouteId(50)).thenReturn(List.of(line, other));

        boolean done = service.confirmLine(7, 102, 3);

        verify(inventoryService).move(77, 5, RECEIVING_CELL, 102, 100, 3, MovementType.PLACEMENT);
        assertThat(line.getLocationId()).isEqualTo(102);
        assertThat(done).isFalse();
    }

    @Test
    void confirmRequiresRouteInProgressAndStorageCell() {
        route(50, PlacementRouteStatus.CREATED);
        PlacementRouteLineEntity line = new PlacementRouteLineEntity(7, 50, 100, 101, 77, 5, false, false);
        when(routeLineRepository.findById(7)).thenReturn(Optional.of(line));
        assertThatThrownBy(() -> service.confirmLine(7, 0, 3)).hasMessageContaining("В работе");

        route(50, PlacementRouteStatus.IN_PROGRESS);
        assertThatThrownBy(() -> service.confirmLine(7, RECEIVING_CELL, 3))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("не является местом хранения");
        verify(inventoryService, never()).move(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void startAndCancelRoute() {
        PlacementRouteEntity route = route(50, PlacementRouteStatus.CREATED);
        service.startRoute(50, 3);
        assertThat(route.getStatus()).isEqualTo(PlacementRouteStatus.IN_PROGRESS.getCode());
        assertThat(route.getEmployerId()).isEqualTo(3);
        assertThatThrownBy(() -> service.startRoute(50, 3)).isInstanceOf(WmsException.class);

        ReceivingLineEntity rl = receivingLine(11);
        rl.setPlacementRoute(true);
        when(lineRepository.findById(11)).thenReturn(Optional.of(rl));
        when(routeLineRepository.findByRouteId(50)).thenReturn(List.of(new PlacementRouteLineEntity(7, 50, 100, 101, 77, 5, false, false)));
        when(batchRepository.findById(77)).thenReturn(Optional.of(new BatchEntity(77, 5, 1, null, 100, null, 11, false)));

        service.cancelRoute(50);

        assertThat(route.getStatus()).isEqualTo(PlacementRouteStatus.CANCELLED.getCode());
        assertThat(rl.getPlacementRoute()).isFalse();
    }

    @Test
    void cannotCancelPartlyPlacedRoute() {
        route(50, PlacementRouteStatus.IN_PROGRESS);
        when(routeLineRepository.findByRouteId(50)).thenReturn(List.of(new PlacementRouteLineEntity(7, 50, 100, 101, 77, 5, true, false)));
        assertThatThrownBy(() -> service.cancelRoute(50)).hasMessageContaining("уже размещена");
        verify(lineRepository, times(0)).save(any());
    }

    // ---------- вспомогательные ----------

    private PlacementRouteEntity route(int id, PlacementRouteStatus status) {
        PlacementRouteEntity r = new PlacementRouteEntity();
        r.setPRouteId(id);
        r.setStatus(status.getCode());
        r.setDel(false);
        when(routeRepository.findById(id)).thenReturn(Optional.of(r));
        return r;
    }

    private static ReceivingLineEntity receivingLine(int id) {
        return new ReceivingLineEntity(id, 100, 100, null, 1, 0, 5, true, false, false, "");
    }

    private static ZoneEntity zone(int id, String code) {
        ZoneEntity z = new ZoneEntity();
        z.setZoneId(id);
        z.setCode(code);
        z.setZoneName(code);
        z.setDel(false);
        return z;
    }

    private static LocationEntity cell(int id, String code, int zoneId, float capacity) {
        return new LocationEntity(id, code, code.substring(4, 6), Integer.parseInt(code.substring(7, 9)), 1, 1,
                capacity, 1, 0, zoneId);
    }

    private static ProductEntity product(int id, int abcId, float volume) {
        ProductEntity p = new ProductEntity();
        p.setProductID(id);
        p.setAbcID(abcId);
        p.setVolume(volume);
        return p;
    }

    private static UnplacedStockRow row(int batchId, int productId, int qty, int targetLocation, int receivingLineId) {
        return new UnplacedStockRow() {
            public Integer getStockId() { return 1; }
            public Integer getBatchId() { return batchId; }
            public Integer getProductId() { return productId; }
            public Integer getQuantity() { return qty; }
            public Integer getTargetLocationId() { return targetLocation; }
            public Integer getReceivingLineId() { return receivingLineId; }
        };
    }
}
