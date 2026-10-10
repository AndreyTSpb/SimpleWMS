package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.StockEntity;
import com.tynyany.simplewmsv2.dao.StockMovementEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.StockMovementRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultInventoryService implements InventoryService {

    /** Значение from/to в журнале, когда товар приходит извне или уходит со склада. */
    private static final int NO_LOCATION = 0;

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LocationRepository locationRepository;
    private final WmsProperties wmsProperties;

    @Override
    @Transactional
    public void receive(int batchId, int productId, int toLocationId, int quantity, int employeeId) {
        checkQuantity(quantity);
        increase(batchId, productId, toLocationId, quantity);
        writeMovement(batchId, NO_LOCATION, toLocationId, quantity, employeeId, MovementType.RECEIVING);
        refreshAvailability(toLocationId);
        log.debug("Приёмка: партия {}, товар {}, ячейка {}, кол-во {}", batchId, productId, toLocationId, quantity);
    }

    @Override
    @Transactional
    public void move(int batchId, int productId, int fromLocationId, int toLocationId,
                     int quantity, int employeeId, MovementType type) {
        checkQuantity(quantity);
        if (fromLocationId == toLocationId) {
            throw new WmsException("Ячейка-источник и ячейка-получатель совпадают");
        }
        decrease(batchId, fromLocationId, quantity);
        increase(batchId, productId, toLocationId, quantity);
        writeMovement(batchId, fromLocationId, toLocationId, quantity, employeeId, type);
        refreshAvailability(fromLocationId);
        refreshAvailability(toLocationId);
        log.debug("{}: партия {}, {} -> {}, кол-во {}", type.getTitle(), batchId, fromLocationId, toLocationId, quantity);
    }

    @Override
    @Transactional
    public void writeOff(int batchId, int fromLocationId, int quantity, int employeeId, MovementType type) {
        checkQuantity(quantity);
        decrease(batchId, fromLocationId, quantity);
        writeMovement(batchId, fromLocationId, NO_LOCATION, quantity, employeeId, type);
        refreshAvailability(fromLocationId);
        log.debug("{}: списание партии {} из ячейки {}, кол-во {}", type.getTitle(), batchId, fromLocationId, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public int getQuantity(int batchId, int locationId) {
        StockEntity stock = stockRepository.findByBatchIdAndLocationIdAndDel(batchId, locationId, false);
        return stock == null ? 0 : stock.getQuantity();
    }

    /**
     * Признак занятости ячейки хранения (location.available): 0 — в ячейке есть товар, 1 — свободна.
     * Для системных ячеек приёмки и отгрузки не ведётся.
     */
    private void refreshAvailability(int locationId) {
        WmsProperties.Locations system = wmsProperties.getLocations();
        if (locationId <= 0 || locationId == system.getReceivingId() || locationId == system.getShippingId()) {
            return;
        }
        locationRepository.findById(locationId).ifPresent(location -> {
            int available = stockRepository.sumQuantityInLocation(locationId) > 0 ? 0 : 1;
            if (location.getAvailable() != available) {
                location.setAvailable(available);
                locationRepository.save(location);
            }
        });
    }

    private void checkQuantity(int quantity) {
        if (quantity <= 0) {
            throw new WmsException("Количество должно быть больше нуля");
        }
    }

    private void decrease(int batchId, int locationId, int quantity) {
        StockEntity stock = stockRepository.findByBatchIdAndLocationIdAndDel(batchId, locationId, false);
        int available = stock == null ? 0 : stock.getQuantity();
        if (available < quantity) {
            throw new WmsException("Недостаточно остатка партии " + batchId + " в ячейке " + locationId
                    + ": доступно " + available + ", требуется " + quantity);
        }
        stock.setQuantity(available - quantity);
        stockRepository.save(stock);
    }

    private void increase(int batchId, int productId, int locationId, int quantity) {
        StockEntity stock = stockRepository.findByBatchIdAndLocationIdAndDel(batchId, locationId, false);
        if (stock == null) {
            stock = new StockEntity(0, productId, batchId, locationId, quantity, false);
        } else {
            stock.setQuantity(stock.getQuantity() + quantity);
        }
        stockRepository.save(stock);
    }

    private void writeMovement(int batchId, int fromLocationId, int toLocationId, int quantity,
                               int employeeId, MovementType type) {
        stockMovementRepository.save(new StockMovementEntity(
                0,
                batchId,
                fromLocationId,
                toLocationId,
                quantity,
                new Timestamp(System.currentTimeMillis()),
                employeeId,
                type.getCode(),
                0,
                false
        ));
    }
}
