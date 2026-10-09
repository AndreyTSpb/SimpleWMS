package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.domain.ReceivingStatus;
import com.tynyany.simplewmsv2.entity.ReceivingJSON;
import com.tynyany.simplewmsv2.entity.ReceivingLineERP;
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
    public ErpImportResult createFromErp(ReceivingJSON json) {
        if (json == null || json.header == null) {
            throw new WmsException("В накладной нет заголовка (header)");
        }
        if (isBlank(json.header.orderERP)) {
            throw new WmsException("Не указан номер накладной (header.orderERP)");
        }
        if (json.body == null || json.body.isEmpty()) {
            throw new WmsException("В накладной нет строк (body)");
        }

        SupplierEntity supplier = supplierRepository.findTopBySupplierCode(json.header.supplierCode)
                .orElseThrow(() -> new NotFoundException("Поставщик с кодом " + json.header.supplierCode + " не найден"));

        Timestamp orderDate;
        try {
            orderDate = Timestamp.valueOf(json.header.orderDate);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new WmsException("Неверная дата накладной (ожидается yyyy-MM-dd HH:mm:ss): " + json.header.orderDate);
        }

        // Сначала проверяем все строки, затем сохраняем: накладная создаётся только целиком
        List<ReceivingLineEntity> lines = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (ReceivingLineERP line : json.body) {
            int qnt = parseQuantity(line);
            Optional<ProductEntity> product = productRepository.findOneByProductCode(line.code);
            if (product.isEmpty()) {
                missing.add(line.code);
                continue;
            }
            lines.add(new ReceivingLineEntity(0, qnt, 0, null, 0, 0,
                    product.get().getProductID(), false, false, false, ""));
        }
        if (lines.isEmpty()) {
            throw new WmsException("Ни один товар из накладной не найден в справочнике: " + String.join(", ", missing));
        }

        ReceivingEntity receiving = receivingRepository.save(new ReceivingEntity(
                0, orderDate, null, json.header.orderERP,
                ReceivingStatus.WAITING.getCode(), 0, supplier.getSupplierID(), false));

        for (ReceivingLineEntity line : lines) {
            line.setReceivingID(receiving.getReceivingID());
            receivingLineRepository.save(line);
        }

        if (!missing.isEmpty()) {
            log.warn("Накладная {}: товары не найдены в справочнике и пропущены: {}", json.header.orderERP, missing);
        }
        log.info("Создана накладная id={} ({}), строк: {}", receiving.getReceivingID(), json.header.orderERP, lines.size());
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
        receiving.setStatusID(ReceivingStatus.COMPLETED.getCode());
        if (receiving.getGetReceivingDate() == null) {
            receiving.setGetReceivingDate(now());
        }
        receivingRepository.save(receiving);
        log.info("Завершена приёмка накладной id={}", receivingId);
    }

    @Override
    @Transactional
    public void processLine(ReceivingLineForm form) {
        ReceivingLineEntity line = receivingLineRepository.findById(form.getNumLine())
                .orElseThrow(() -> new NotFoundException("Строка прихода " + form.getNumLine() + " не найдена"));
        ReceivingEntity receiving = getReceiving(line.getReceivingID());

        if (form.getOrderId() > 0 && form.getOrderId() != receiving.getReceivingID()) {
            throw new WmsException("Строка " + line.getReceivingLineID() + " не относится к накладной " + form.getOrderId());
        }
        requireStatus(receiving, ReceivingStatus.IN_PROGRESS, "Принять строку");
        if (Boolean.TRUE.equals(line.getComplete())) {
            throw new WmsException("Строка " + line.getReceivingLineID() + " уже принята, повторная приёмка не допускается");
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

        if (qntFact > 0) {
            checkStorageLocation(form.getLocationId());
        }

        line.setQuantityReceived(qntFact);
        line.setExpirationDate(expirationDate);
        line.setLocationID(form.getLocationId());
        line.setNote(form.getNote());
        line.setComplete(true);
        receivingLineRepository.save(line);

        if (qntFact == 0) {
            log.info("Строка {} накладной {}: товар не поступил", line.getReceivingLineID(), receiving.getReceivingID());
            return;
        }

        // Партия ссылается на строку прихода (batches.receiving_id = receiving_line.receiving_lineid)
        BatchEntity batch = batchRepository.save(new BatchEntity(
                0, product.getProductID(), receiving.getSupplierID(), now(), qntFact,
                expirationDate, line.getReceivingLineID(), false));

        inventoryService.receive(batch.getBatch_id(), product.getProductID(),
                wmsProperties.getLocations().getReceivingId(), qntFact, receiving.getEmployeeID());

        log.info("Принята строка {} накладной {}: товар {}, кол-во {}, партия {}",
                line.getReceivingLineID(), receiving.getReceivingID(), product.getProductID(), qntFact, batch.getBatch_id());
    }

    /**
     * Ячейка, куда товар будет размещён после приёмки: должна существовать
     * и не быть системной ячейкой приёмки/отгрузки.
     */
    private void checkStorageLocation(int locationId) {
        WmsProperties.Locations system = wmsProperties.getLocations();
        if (locationId <= 0 || locationId == system.getReceivingId() || locationId == system.getShippingId()) {
            throw new WmsException("Выберите место хранения, куда будет размещён товар");
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

    private int parseQuantity(ReceivingLineERP line) {
        try {
            int qnt = Integer.parseInt(line.qnt.trim());
            if (qnt <= 0) throw new NumberFormatException();
            return qnt;
        } catch (NumberFormatException | NullPointerException e) {
            throw new WmsException("Неверное количество для товара " + line.code + ": " + line.qnt);
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
