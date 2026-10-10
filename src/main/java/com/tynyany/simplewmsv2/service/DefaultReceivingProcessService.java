package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.ReceivingStatus;
import com.tynyany.simplewmsv2.dto.erp.ErpLine;
import com.tynyany.simplewmsv2.dto.erp.ErpReceiving;
import com.tynyany.simplewmsv2.entity.ReceivingLineForm;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultReceivingProcessService implements ReceivingProcessService {

    private final ReceivingRepository receivingRepository;
    private final ReceivingLineRepository receivingLineRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final BatchRepository batchRepository;
    private final LocationRepository locationRepository;
    private final InventoryService inventoryService;
    private final WmsProperties wmsProperties;

    @Override
    @Transactional
    public ErpImportResult createFromErp(ErpReceiving request) {
        if (request == null || isBlank(request.number())) {
            throw new WmsException("Не указан номер накладной");
        }
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new WmsException("В накладной нет строк");
        }
        String number = request.number().trim();
        receivingRepository.findFirstByDocumentNumberAndStatusIDNot(number, ReceivingStatus.CANCELLED.getCode())
                .ifPresent(existing -> {
                    throw new WmsException("Накладная " + number + " уже загружена (id "
                            + existing.getReceivingID() + "). Повторная загрузка возможна после её отмены");
                });

        SupplierEntity supplier = supplierRepository.findTopBySupplierCode(request.supplierCode())
                .orElseThrow(() -> new NotFoundException("Поставщик с кодом " + request.supplierCode() + " не найден"));
        Timestamp orderDate = request.date() != null ? Timestamp.valueOf(request.date()) : now();

        // Сначала проверяем все строки, затем сохраняем: накладная создаётся только целиком
        List<ReceivingLineEntity> lines = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (ErpLine line : request.lines()) {
            if (line.quantity() <= 0) {
                throw new WmsException("Неверное количество для товара " + line.code() + ": " + line.quantity());
            }
            int qnt = line.quantity();
            Optional<ProductEntity> product = productRepository.findOneByProductCode(line.code());
            if (product.isEmpty()) {
                missing.add(line.code());
                continue;
            }
            lines.add(new ReceivingLineEntity(0, qnt, 0, null, 0, 0,
                    product.get().getProductID(), false, false, false, ""));
        }
        if (lines.isEmpty()) {
            throw new WmsException("Ни один товар из накладной не найден в справочнике: " + String.join(", ", missing));
        }

        ReceivingEntity receiving = receivingRepository.save(new ReceivingEntity(
                0, orderDate, null, number,
                ReceivingStatus.WAITING.getCode(), 0, supplier.getSupplierID(), false));

        for (ReceivingLineEntity line : lines) {
            line.setReceivingID(receiving.getReceivingID());
            receivingLineRepository.save(line);
        }

        if (!missing.isEmpty()) {
            log.warn("Накладная {}: товары не найдены в справочнике и пропущены: {}", number, missing);
        }
        log.info("Создана накладная id={} ({}), строк: {}", receiving.getReceivingID(), number, lines.size());
        return new ErpImportResult(receiving.getReceivingID(), lines.size(), missing);
    }

    @Override
    @Transactional
    public void start(int receivingId, int employeeId, Timestamp startDate) {
        ReceivingEntity receiving = getReceiving(receivingId);
        requireStatus(receiving, ReceivingStatus.WAITING, "Начать приёмку");
        if (employeeId <= 0) {
            throw new WmsException("Не выбран кладовщик");
        }
        receiving.setStatusID(ReceivingStatus.IN_PROGRESS.getCode());
        receiving.setEmployeeID(employeeId);
        receiving.setGetReceivingDate(startDate != null ? startDate : now());
        receivingRepository.save(receiving);
        log.info("Начата приёмка накладной id={}, кладовщик id={}", receivingId, employeeId);
    }

    @Override
    @Transactional
    public void complete(int receivingId) {
        ReceivingEntity receiving = getReceiving(receivingId);
        requireStatus(receiving, ReceivingStatus.IN_PROGRESS, "Завершить приёмку");
        long notProcessed = activeLines(receivingId).stream()
                .filter(line -> !Boolean.TRUE.equals(line.getComplete()))
                .count();
        if (notProcessed > 0) {
            throw new WmsException("Нельзя завершить приёмку: не обработано строк — " + notProcessed);
        }
        receiving.setStatusID(ReceivingStatus.COMPLETED.getCode());
        if (receiving.getGetReceivingDate() == null) {
            receiving.setGetReceivingDate(now());
        }
        receivingRepository.save(receiving);
        log.info("Завершена приёмка накладной id={}", receivingId);
    }

    @Override
    @Transactional
    public void cancel(int receivingId, int executorEmployeeId) {
        ReceivingEntity receiving = getReceiving(receivingId);
        ReceivingStatus status = ReceivingStatus.fromCode(receiving.getStatusID());
        if (status != ReceivingStatus.WAITING && status != ReceivingStatus.IN_PROGRESS) {
            throw new WmsException("Отменить можно только накладную в статусе «" + ReceivingStatus.WAITING.getTitle()
                    + "» или «" + ReceivingStatus.IN_PROGRESS.getTitle() + "», текущий статус: «" + status.getTitle() + "»");
        }
        List<ReceivingLineEntity> lines = activeLines(receivingId);
        for (ReceivingLineEntity line : lines) {
            if (Boolean.TRUE.equals(line.getPlacementRoute())) {
                throw new WmsException("Нельзя отменить накладную: " + lineLabel(line) + " уже включена в маршрут размещения");
            }
        }
        int employeeId = executorEmployeeId > 0 ? executorEmployeeId : receiving.getEmployeeID();
        for (ReceivingLineEntity line : lines) {
            if (Boolean.TRUE.equals(line.getComplete())) {
                undoLine(line, employeeId);
            }
        }
        receiving.setStatusID(ReceivingStatus.CANCELLED.getCode());
        receivingRepository.save(receiving);
        log.info("Отменена накладная id={} ({})", receivingId, receiving.getDocumentNumber());
    }

    @Override
    @Transactional
    public String revertLine(int receivingLineId, int executorEmployeeId) {
        ReceivingLineEntity line = receivingLineRepository.findById(receivingLineId)
                .orElseThrow(() -> new NotFoundException("Строка прихода " + receivingLineId + " не найдена"));
        ReceivingEntity receiving = getReceiving(line.getReceivingID());
        requireStatus(receiving, ReceivingStatus.IN_PROGRESS, "Отменить приёмку строки");
        if (!Boolean.TRUE.equals(line.getComplete())) {
            throw new WmsException(capitalize(lineLabel(line)) + " ещё не принята");
        }
        if (Boolean.TRUE.equals(line.getPlacementRoute())) {
            throw new WmsException(capitalize(lineLabel(line)) + " уже включена в маршрут размещения, отмена невозможна");
        }
        undoLine(line, executorEmployeeId > 0 ? executorEmployeeId : receiving.getEmployeeID());
        log.info("Отменена приёмка строки {} накладной {}", receivingLineId, receiving.getReceivingID());
        return lineLabel(line);
    }

    /** Откат принятой строки: списание остатка из ячейки приёмки, удаление партии, сброс факта. */
    private void undoLine(ReceivingLineEntity line, int employeeId) {
        int receivingLocationId = wmsProperties.getLocations().getReceivingId();
        for (BatchEntity batch : batchRepository.findActiveByReceivingLine(line.getReceivingLineID())) {
            int inReceiving = inventoryService.getQuantity(batch.getBatch_id(), receivingLocationId);
            if (inReceiving < batch.getQuantity_received()) {
                throw new WmsException(capitalize(lineLabel(line)) + ": часть товара партии "
                        + batch.getBatch_id() + " уже перемещена из зоны приёмки, отмена невозможна");
            }
            if (inReceiving > 0) {
                inventoryService.writeOff(batch.getBatch_id(), receivingLocationId, inReceiving,
                        employeeId, MovementType.ADJUSTMENT);
            }
            batch.setDel(true);
            batchRepository.save(batch);
        }
        line.setComplete(false);
        line.setQuantityReceived(0);
        line.setExpirationDate(null);
        receivingLineRepository.save(line);
    }

    /**
     * Обозначение строки для сообщений: порядковый номер в накладной (как в таблице на странице) и товар.
     * Внутренний id строки пользователю не показывается.
     */
    private String lineLabel(ReceivingLineEntity line) {
        List<ReceivingLineEntity> lines = activeLines(line.getReceivingID());
        int position = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getReceivingLineID() == line.getReceivingLineID()) {
                position = i + 1;
                break;
            }
        }
        String product = productRepository.findById(line.getProductID())
                .map(ProductEntity::getProductName)
                .orElse("товар " + line.getProductID());
        return (position > 0 ? "строка " + position : "строка") + " «" + product + "»";
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private List<ReceivingLineEntity> activeLines(int receivingId) {
        return receivingLineRepository.findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(receivingId, false);
    }

    @Override
    @Transactional
    public String processLine(ReceivingLineForm form, int executorEmployeeId) {
        ReceivingLineEntity line = receivingLineRepository.findById(form.getNumLine())
                .orElseThrow(() -> new NotFoundException("Строка прихода " + form.getNumLine() + " не найдена"));
        ReceivingEntity receiving = getReceiving(line.getReceivingID());

        if (form.getOrderId() > 0 && form.getOrderId() != receiving.getReceivingID()) {
            throw new WmsException("Строка прихода не относится к накладной " + form.getOrderId());
        }
        requireStatus(receiving, ReceivingStatus.IN_PROGRESS, "Принять строку");
        if (Boolean.TRUE.equals(line.getComplete())) {
            throw new WmsException(capitalize(lineLabel(line)) + " уже принята, повторная приёмка не допускается");
        }

        int qntFact = form.getQntFact();
        if (qntFact < 0) {
            throw new WmsException("Фактическое количество не может быть отрицательным");
        }

        ProductEntity product = productRepository.findById(line.getProductID())
                .orElseThrow(() -> new NotFoundException("Товар " + line.getProductID() + " не найден"));
        Timestamp expirationDate = parseDate(form.getExpirationDate());
        if (qntFact > 0 && Boolean.TRUE.equals(product.getExpirationDateRequired()) && expirationDate == null) {
            throw new WmsException("Для товара «" + product.getProductName() + "» обязателен срок годности");
        }

        // Место хранения необязательно: без него ячейка подбирается при формировании маршрута
        if (qntFact > 0 && form.getLocationId() > 0) {
            checkStorageLocation(form.getLocationId());
        }

        line.setQuantityReceived(qntFact);
        line.setExpirationDate(expirationDate);
        line.setLocationID(Math.max(form.getLocationId(), 0));
        line.setNote(form.getNote());
        line.setComplete(true);
        receivingLineRepository.save(line);

        if (qntFact == 0) {
            log.info("Строка {} накладной {}: товар не поступил", line.getReceivingLineID(), receiving.getReceivingID());
            return lineLabel(line);
        }

        // Партия ссылается на строку прихода (batches.receiving_id = receiving_line.receiving_lineid)
        BatchEntity batch = batchRepository.save(new BatchEntity(
                0, product.getProductID(), receiving.getSupplierID(), now(), qntFact,
                expirationDate, line.getReceivingLineID(), false));

        inventoryService.receive(batch.getBatch_id(), product.getProductID(),
                wmsProperties.getLocations().getReceivingId(), qntFact,
                executorEmployeeId > 0 ? executorEmployeeId : receiving.getEmployeeID());

        log.info("Принята строка {} накладной {}: товар {}, кол-во {}, партия {}",
                line.getReceivingLineID(), receiving.getReceivingID(), product.getProductID(), qntFact, batch.getBatch_id());
        return lineLabel(line);
    }

    /**
     * Ячейка, куда товар будет размещён после приёмки: должна существовать
     * и не быть системной ячейкой приёмки/отгрузки.
     */
    private void checkStorageLocation(int locationId) {
        WmsProperties.Locations system = wmsProperties.getLocations();
        if (locationId == system.getReceivingId() || locationId == system.getShippingId()) {
            throw new WmsException("Ячейки приёмки и отгрузки не являются местом хранения");
        }
        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Место хранения " + locationId + " не найдено"));
        if (location.getDel() != 0) {
            throw new WmsException("Место хранения " + location.getLocationCode() + " удалено");
        }
    }

    private ReceivingEntity getReceiving(int receivingId) {
        return receivingRepository.findById(receivingId)
                .orElseThrow(() -> new NotFoundException("Приходная накладная " + receivingId + " не найдена"));
    }

    private void requireStatus(ReceivingEntity receiving, ReceivingStatus expected, String action) {
        ReceivingStatus actual = ReceivingStatus.fromCode(receiving.getStatusID());
        if (actual != expected) {
            throw new WmsException(action + " можно только для накладной в статусе «" + expected.getTitle()
                    + "», текущий статус: «" + actual.getTitle() + "»");
        }
    }

    private static Timestamp parseDate(String value) {
        if (isBlank(value) || "null".equals(value)) return null;
        try {
            return Timestamp.valueOf(LocalDate.parse(value.trim()).atStartOfDay());
        } catch (DateTimeParseException e) {
            throw new WmsException("Неверный формат даты (ожидается ГГГГ-ММ-ДД): " + value);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
