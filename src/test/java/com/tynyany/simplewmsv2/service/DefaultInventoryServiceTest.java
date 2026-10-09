package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.StockEntity;
import com.tynyany.simplewmsv2.dao.StockMovementEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.StockMovementRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultInventoryServiceTest {

    private static final int BATCH = 10;
    private static final int PRODUCT = 5;
    private static final int RECEIVING_CELL = 1;
    private static final int STORAGE_CELL = 101;

    private StockRepository stockRepository;
    private StockMovementRepository movementRepository;
    private DefaultInventoryService service;

    @BeforeEach
    void setUp() {
        stockRepository = mock(StockRepository.class);
        movementRepository = mock(StockMovementRepository.class);
        service = new DefaultInventoryService(stockRepository, movementRepository);
    }

    @Test
    void receiveCreatesStockAndMovement() {
        service.receive(BATCH, PRODUCT, RECEIVING_CELL, 100, 3);

        ArgumentCaptor<StockEntity> stock = ArgumentCaptor.forClass(StockEntity.class);
        verify(stockRepository).save(stock.capture());
        assertThat(stock.getValue().getQuantity()).isEqualTo(100);
        assertThat(stock.getValue().getLocationId()).isEqualTo(RECEIVING_CELL);
        assertThat(stock.getValue().getProduct_id()).isEqualTo(PRODUCT);

        ArgumentCaptor<StockMovementEntity> movement = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(movementRepository).save(movement.capture());
        assertThat(movement.getValue().getMovementTypeId()).isEqualTo(MovementType.RECEIVING.getCode());
        assertThat(movement.getValue().getFromLocationId()).isZero();
        assertThat(movement.getValue().getToLocationId()).isEqualTo(RECEIVING_CELL);
        assertThat(movement.getValue().getQuantity()).isEqualTo(100);
    }

    @Test
    void receiveAddsToExistingStock() {
        StockEntity existing = new StockEntity(7, PRODUCT, BATCH, RECEIVING_CELL, 40, false);
        when(stockRepository.findByBatchIdAndLocationIdAndDel(BATCH, RECEIVING_CELL, false)).thenReturn(existing);

        service.receive(BATCH, PRODUCT, RECEIVING_CELL, 60, 3);

        assertThat(existing.getQuantity()).isEqualTo(100);
        verify(stockRepository).save(existing);
    }

    @Test
    void moveTransfersQuantityBetweenCells() {
        StockEntity from = new StockEntity(7, PRODUCT, BATCH, RECEIVING_CELL, 100, false);
        StockEntity to = new StockEntity(8, PRODUCT, BATCH, STORAGE_CELL, 5, false);
        when(stockRepository.findByBatchIdAndLocationIdAndDel(BATCH, RECEIVING_CELL, false)).thenReturn(from);
        when(stockRepository.findByBatchIdAndLocationIdAndDel(BATCH, STORAGE_CELL, false)).thenReturn(to);

        service.move(BATCH, PRODUCT, RECEIVING_CELL, STORAGE_CELL, 30, 3, MovementType.PLACEMENT);

        assertThat(from.getQuantity()).isEqualTo(70);
        assertThat(to.getQuantity()).isEqualTo(35);
        ArgumentCaptor<StockMovementEntity> movement = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(movementRepository).save(movement.capture());
        assertThat(movement.getValue().getMovementTypeId()).isEqualTo(MovementType.PLACEMENT.getCode());
        assertThat(movement.getValue().getFromLocationId()).isEqualTo(RECEIVING_CELL);
        assertThat(movement.getValue().getToLocationId()).isEqualTo(STORAGE_CELL);
    }

    @Test
    void moveFailsWhenNotEnoughStock() {
        StockEntity from = new StockEntity(7, PRODUCT, BATCH, RECEIVING_CELL, 10, false);
        when(stockRepository.findByBatchIdAndLocationIdAndDel(BATCH, RECEIVING_CELL, false)).thenReturn(from);

        assertThatThrownBy(() -> service.move(BATCH, PRODUCT, RECEIVING_CELL, STORAGE_CELL, 11, 3, MovementType.PLACEMENT))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("Недостаточно остатка");
        assertThat(from.getQuantity()).isEqualTo(10);
        verify(movementRepository, never()).save(any());
    }

    @Test
    void moveFailsWhenNoStockInSourceCell() {
        assertThatThrownBy(() -> service.move(BATCH, PRODUCT, RECEIVING_CELL, STORAGE_CELL, 1, 3, MovementType.PLACEMENT))
                .isInstanceOf(WmsException.class);
        verify(stockRepository, never()).save(any());
    }

    @Test
    void writeOffDecreasesStockAndRecordsMovementToNowhere() {
        StockEntity stock = new StockEntity(7, PRODUCT, BATCH, RECEIVING_CELL, 100, false);
        when(stockRepository.findByBatchIdAndLocationIdAndDel(BATCH, RECEIVING_CELL, false)).thenReturn(stock);

        service.writeOff(BATCH, RECEIVING_CELL, 100, 3, MovementType.ADJUSTMENT);

        assertThat(stock.getQuantity()).isZero();
        ArgumentCaptor<StockMovementEntity> movement = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(movementRepository).save(movement.capture());
        assertThat(movement.getValue().getMovementTypeId()).isEqualTo(MovementType.ADJUSTMENT.getCode());
        assertThat(movement.getValue().getFromLocationId()).isEqualTo(RECEIVING_CELL);
        assertThat(movement.getValue().getToLocationId()).isZero();
        assertThatThrownBy(() -> service.writeOff(BATCH, RECEIVING_CELL, 1, 3, MovementType.ADJUSTMENT))
                .isInstanceOf(WmsException.class);
    }

    @Test
    void zeroOrNegativeQuantityIsRejected() {
        assertThatThrownBy(() -> service.receive(BATCH, PRODUCT, RECEIVING_CELL, 0, 3)).isInstanceOf(WmsException.class);
        assertThatThrownBy(() -> service.move(BATCH, PRODUCT, RECEIVING_CELL, STORAGE_CELL, -5, 3, MovementType.PLACEMENT))
                .isInstanceOf(WmsException.class);
        verify(stockRepository, never()).save(any());
    }

    @Test
    void moveToSameCellIsRejected() {
        assertThatThrownBy(() -> service.move(BATCH, PRODUCT, STORAGE_CELL, STORAGE_CELL, 1, 3, MovementType.PLACEMENT))
                .isInstanceOf(WmsException.class);
    }

    @Test
    void getQuantityReturnsZeroWhenNoStock() {
        when(stockRepository.findByBatchIdAndLocationIdAndDel(anyInt(), anyInt(), eq(false))).thenReturn(null);
        assertThat(service.getQuantity(BATCH, STORAGE_CELL)).isZero();
    }
}
