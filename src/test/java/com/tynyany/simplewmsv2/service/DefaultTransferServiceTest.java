package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockMovementRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultTransferServiceTest {

    private InventoryService inventoryService;
    private StockRepository stockRepository;
    private StockReservationRepository reservationRepository;
    private DefaultTransferService service;

    @BeforeEach
    void setUp() {
        inventoryService = mock(InventoryService.class);
        stockRepository = mock(StockRepository.class);
        reservationRepository = mock(StockReservationRepository.class);
        LocationRepository locationRepository = mock(LocationRepository.class);
        BatchRepository batchRepository = mock(BatchRepository.class);
        WmsProperties properties = new WmsProperties();
        properties.getLocations().setReceivingId(1);
        properties.getLocations().setShippingId(2);
        service = new DefaultTransferService(inventoryService, stockRepository, mock(StockMovementRepository.class),
                locationRepository, batchRepository, mock(ProductRepository.class), mock(EmployeeRepository.class),
                reservationRepository, properties);

        for (LocationEntity l : new LocationEntity[]{
                new LocationEntity(1, "PRM-01-01-1-1", "01", 1, 1, 1, 1000, 1, 0, 1),
                new LocationEntity(101, "SC1-01-01-1-1", "01", 1, 1, 1, 100, 0, 0, 3),
                new LocationEntity(102, "SC1-01-01-2-1", "01", 1, 2, 1, 100, 1, 0, 3),
                new LocationEntity(103, "SC1-01-02-1-1", "01", 2, 1, 1, 100, 1, 1, 3)}) {
            when(locationRepository.findById(l.getLocationID())).thenReturn(Optional.of(l));
        }
        when(batchRepository.findById(77)).thenReturn(Optional.of(new BatchEntity(77, 5, 1, null, 100, null, 11, false)));
    }

    @Test
    void transferMovesStockWithMoveType() {
        when(stockRepository.sumVolumeInLocation(102)).thenReturn(40.0);

        TransferService.TransferResult result = service.transfer(77, 101, 102, 30, 3);

        verify(inventoryService).move(77, 5, 101, 102, 30, 3, MovementType.MOVE);
        assertThat(result.overCapacity()).isFalse();
        assertThat(result.targetCode()).isEqualTo("SC1-01-01-2-1");
    }

    @Test
    void overCapacityIsWarningNotError() {
        when(stockRepository.sumVolumeInLocation(102)).thenReturn(130.0);
        TransferService.TransferResult result = service.transfer(77, 101, 102, 30, 3);
        assertThat(result.overCapacity()).isTrue();
        verify(inventoryService).move(77, 5, 101, 102, 30, 3, MovementType.MOVE);
    }

    @Test
    void systemDeletedAndSameCellsAreRejected() {
        assertThatThrownBy(() -> service.transfer(77, 1, 102, 1, 3)).hasMessageContaining("системная ячейка");
        assertThatThrownBy(() -> service.transfer(77, 101, 1, 1, 3)).hasMessageContaining("системная ячейка");
        assertThatThrownBy(() -> service.transfer(77, 101, 103, 1, 3)).hasMessageContaining("удалена");
        assertThatThrownBy(() -> service.transfer(77, 101, 101, 1, 3)).hasMessageContaining("совпадают");
        verify(inventoryService, never()).move(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void notEnoughStockErrorFromInventoryIsPropagated() {
        doThrow(new WmsException("Недостаточно остатка")).when(inventoryService)
                .move(77, 5, 101, 102, 500, 3, MovementType.MOVE);
        assertThatThrownBy(() -> service.transfer(77, 101, 102, 500, 3)).hasMessageContaining("Недостаточно остатка");
    }

    @Test
    void reservedQuantityCannotBeMoved() {
        when(reservationRepository.sumReserved(77, 101)).thenReturn(60);
        when(inventoryService.getQuantity(77, 101)).thenReturn(100);

        assertThatThrownBy(() -> service.transfer(77, 101, 102, 41, 3))
                .hasMessageContaining("свободно 40 шт.")
                .hasMessageContaining("60 шт. зарезервировано");
        verify(inventoryService, never()).move(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());

        service.transfer(77, 101, 102, 40, 3);
        verify(inventoryService).move(77, 5, 101, 102, 40, 3, MovementType.MOVE);
    }

    @Test
    void emptySearchReturnsNothing() {
        assertThat(service.findProduct("  ")).isEmpty();
        verify(stockRepository, never()).findPositionsByProduct(any(), any());
    }
}
