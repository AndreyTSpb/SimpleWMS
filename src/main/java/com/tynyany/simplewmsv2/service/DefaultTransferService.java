package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.StockMovementEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.dto.StockPositionRow;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockMovementRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultTransferService implements TransferService {

    private final InventoryService inventoryService;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LocationRepository locationRepository;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final StockReservationRepository reservationRepository;
    private final WmsProperties wmsProperties;

    @Override
    @Transactional
    public TransferResult transfer(int batchId, int fromLocationId, int toLocationId, int quantity, int employeeId) {
        if (fromLocationId == toLocationId) {
            throw new WmsException("Ячейка-источник и ячейка-получатель совпадают");
        }
        LocationEntity from = storageCell(fromLocationId, "Ячейка-источник");
        LocationEntity to = storageCell(toLocationId, "Ячейка-получатель");
        BatchEntity batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new NotFoundException("Партия " + batchId + " не найдена"));

        // Зарезервированное под заказы не перемещается: сборщик будет искать товар в этой ячейке
        int reserved = reservationRepository.sumReserved(batchId, fromLocationId);
        if (reserved > 0) {
            int free = inventoryService.getQuantity(batchId, fromLocationId) - reserved;
            if (quantity > free) {
                throw new WmsException("В ячейке " + from.getLocationCode() + " свободно " + Math.max(free, 0)
                        + " шт. партии " + batchId + ", ещё " + reserved + " шт. зарезервировано под заказы");
            }
        }
        // Проверка количества и остатка — в InventoryService.move (откат всей операции при ошибке)
        inventoryService.move(batchId, batch.getProduct_id(), fromLocationId, toLocationId,
                quantity, employeeId, MovementType.MOVE);

        double used = stockRepository.sumVolumeInLocation(toLocationId);
        boolean over = used > to.getCapacity();
        log.info("Перемещение: партия {}, {} -> {}, кол-во {}{}", batchId, from.getLocationCode(), to.getLocationCode(),
                quantity, over ? " (превышена вместимость ячейки-получателя)" : "");
        return new TransferResult(over, to.getLocationCode(), used, to.getCapacity());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockPositionRow> cellContents(int locationId) {
        return stockRepository.findPositionsInLocation(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockPositionRow> findProduct(String query) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) return List.of();
        return stockRepository.findPositionsByProduct("%" + q + "%", q);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoveView> recentMoves() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        List<MoveView> result = new ArrayList<>();
        for (StockMovementEntity m : stockMovementRepository.findTop30ByMovementTypeIdOrderByMovementIdDesc(MovementType.MOVE.getCode())) {
            Optional<BatchEntity> batch = batchRepository.findById(m.getBatchId());
            var product = batch.flatMap(b -> productRepository.findById(b.getProduct_id()));
            result.add(new MoveView(
                    m.getMovementDate() != null ? format.format(m.getMovementDate()) : "",
                    product.map(p -> p.getProductCode()).orElse("?"),
                    product.map(p -> p.getProductName()).orElse(""),
                    m.getBatchId(),
                    locationCode(m.getFromLocationId()),
                    locationCode(m.getToLocationId()),
                    m.getQuantity(),
                    employeeRepository.findById(m.getEmployeeID()).map(e -> e.getEmployeeName()).orElse("")));
        }
        return result;
    }

    /** Действующая ячейка хранения (не системная и не удалённая). */
    private LocationEntity storageCell(int locationId, String role) {
        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException(role + ": ячейка " + locationId + " не найдена"));
        WmsProperties.Locations system = wmsProperties.getLocations();
        if (locationId == system.getReceivingId() || locationId == system.getShippingId()) {
            throw new WmsException(role + " " + location.getLocationCode()
                    + " — системная ячейка; товар из приёмки размещается маршрутом, в отгрузку попадает при сборке");
        }
        if (location.getDel() != 0) {
            throw new WmsException(role + " " + location.getLocationCode() + " удалена");
        }
        return location;
    }

    private String locationCode(int locationId) {
        return locationRepository.findById(locationId).map(LocationEntity::getLocationCode).orElse(String.valueOf(locationId));
    }
}
