package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.PlacementRouteEntity;
import com.tynyany.simplewmsv2.dao.PlacementRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.domain.PlacementRouteStatus;
import com.tynyany.simplewmsv2.dto.UnplacedStockRow;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.PlacementRouteLineRepository;
import com.tynyany.simplewmsv2.repository.PlacementRouteRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import com.tynyany.simplewmsv2.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultPlacementServiceTest {

    private static final int RECEIVING_CELL = 1;

    private StockRepository stockRepository;
    private ReceivingLineRepository lineRepository;
    private PlacementRouteRepository routeRepository;
    private PlacementRouteLineRepository routeLineRepository;
    private DefaultPlacementService service;

    @BeforeEach
    void setUp() {
        stockRepository = mock(StockRepository.class);
        lineRepository = mock(ReceivingLineRepository.class);
        routeRepository = mock(PlacementRouteRepository.class);
        routeLineRepository = mock(PlacementRouteLineRepository.class);
        WmsProperties properties = new WmsProperties();
        properties.getLocations().setReceivingId(RECEIVING_CELL);
        service = new DefaultPlacementService(stockRepository, lineRepository, routeRepository, routeLineRepository,
                mock(ProductRepository.class), mock(LocationRepository.class), mock(ZoneRepository.class), properties);

        when(routeRepository.save(any())).thenAnswer(inv -> {
            PlacementRouteEntity r = inv.getArgument(0);
            r.setPRouteId(50);
            return r;
        });
    }

    @Test
    void routeLineGetsStockQuantityAndReceivingLineIsMarked() {
        ReceivingLineEntity receivingLine = new ReceivingLineEntity(11, 100, 100, null, 1, 101, 5, true, false, false, "");
        when(lineRepository.findZonesWithUnplacedStock(RECEIVING_CELL)).thenReturn(List.of(3));
        when(stockRepository.findUnplacedInZone(3, RECEIVING_CELL)).thenReturn(List.of(row(77, 5, 100, 101, 11)));
        when(lineRepository.findById(11)).thenReturn(Optional.of(receivingLine));

        PlacementService.GenerationResult result = service.generateRoutes();

        assertThat(result.routesCreated()).isEqualTo(1);
        assertThat(result.linesCreated()).isEqualTo(1);

        ArgumentCaptor<PlacementRouteEntity> route = ArgumentCaptor.forClass(PlacementRouteEntity.class);
        verify(routeRepository).save(route.capture());
        assertThat(route.getValue().getStatus()).isEqualTo(PlacementRouteStatus.CREATED.getCode());

        ArgumentCaptor<PlacementRouteLineEntity> line = ArgumentCaptor.forClass(PlacementRouteLineEntity.class);
        verify(routeLineRepository).save(line.capture());
        assertThat(line.getValue().getPRouteId()).isEqualTo(50);
        assertThat(line.getValue().getQnt()).isEqualTo(100);          // количество, а не id товара
        assertThat(line.getValue().getProductId()).isEqualTo(5);
        assertThat(line.getValue().getBatchId()).isEqualTo(77);
        assertThat(line.getValue().getLocationId()).isEqualTo(101);
        assertThat(receivingLine.getPlacementRoute()).isTrue();
    }

    @Test
    void zonesWithoutTargetCellOrStockAreSkipped() {
        when(lineRepository.findZonesWithUnplacedStock(RECEIVING_CELL)).thenReturn(Arrays.asList(null, 4));
        when(stockRepository.findUnplacedInZone(4, RECEIVING_CELL)).thenReturn(List.of());

        PlacementService.GenerationResult result = service.generateRoutes();

        assertThat(result.routesCreated()).isZero();
        verify(routeRepository, never()).save(any());
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
