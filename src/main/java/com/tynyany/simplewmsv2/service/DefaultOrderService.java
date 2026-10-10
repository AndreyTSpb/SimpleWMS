package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderLineEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.StockReservationEntity;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.dto.AvailableStockRow;
import com.tynyany.simplewmsv2.dto.erp.ErpLine;
import com.tynyany.simplewmsv2.dto.erp.ErpOrder;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderLineRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultOrderService implements OrderService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderLineRepository lineRepository;
    private final StockReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final BatchRepository batchRepository;
    private final WmsProperties wmsProperties;

    // ---------- создание ----------

    @Override
    @Transactional
    public ErpOrderResult createFromErp(ErpOrder request) {
        if (request == null || isBlank(request.number())) {
            throw new WmsException("Не указан номер заказа");
        }
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new WmsException("В заказе нет строк");
        }
        String number = request.number().trim();
        checkUniqueNumber(number);

        CustomerEntity customer = customerRepository.findByCustomerCode(request.customerCode())
                .orElseThrow(() -> new NotFoundException("Клиент с кодом " + request.customerCode() + " не найден"));
        Timestamp orderDate = request.date() != null ? Timestamp.valueOf(request.date()) : now();
        Timestamp shipDate = request.shipDate() != null ? Timestamp.valueOf(request.shipDate().atStartOfDay()) : null;

        // Строки проверяются до сохранения: заказ создаётся целиком
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
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
            quantities.merge(product.get().getProductID(), qnt, Integer::sum);
        }
        if (quantities.isEmpty()) {
            throw new WmsException("Ни один товар из заказа не найден в справочнике: " + String.join(", ", missing));
        }

        CustomerOrderEntity order = orderRepository.save(new CustomerOrderEntity(
                0, number, customer.getCustomerId(), orderDate, shipDate,
                OrderStatus.NEW.getCode(), request.note(), now(), false));
        quantities.forEach((productId, qnt) ->
                lineRepository.save(new CustomerOrderLineEntity(0, order.getOrderId(), productId, qnt, 0, 0, 0)));

        if (!missing.isEmpty()) {
            log.warn("Заказ {}: товары не найдены в справочнике и пропущены: {}", number, missing);
        }
        ReserveResult reserve = reserve(order.getOrderId());
        log.info("Создан заказ id={} ({}), строк: {}, резерв: {}", order.getOrderId(), number, quantities.size(), reserve);
        return new ErpOrderResult(order.getOrderId(), quantities.size(), missing, reserve);
    }

    @Override
    @Transactional
    public int create(int customerId, String documentNumber, String shipDateText, String note) {
        Timestamp shipDate = parseDate(shipDateText);
        customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Клиент " + customerId + " не найден"));
        String number = isBlank(documentNumber) ? null : documentNumber.trim();
        if (number != null) {
            checkUniqueNumber(number);
        }
        CustomerOrderEntity order = orderRepository.save(new CustomerOrderEntity(
                0, number, customerId, now(), shipDate, OrderStatus.NEW.getCode(), note, now(), false));
        if (number == null) {
            order.setDocumentNumber("WMS-" + order.getOrderId());
            orderRepository.save(order);
        }
        log.info("Создан заказ id={} ({}) вручную", order.getOrderId(), order.getDocumentNumber());
        return order.getOrderId();
    }

    @Override
    @Transactional
    public void addLine(int orderId, int productId, int quantity) {
        CustomerOrderEntity order = getOrderEntity(orderId);
        requireStatus(order, "Добавлять строки", OrderStatus.NEW);
        if (quantity <= 0) {
            throw new WmsException("Количество должно быть больше нуля");
        }
        productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар " + productId + " не найден"));
        Optional<CustomerOrderLineEntity> existing = lineRepository.findByOrderIdOrderByLineIdAsc(orderId).stream()
                .filter(l -> l.getProductId() == productId)
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQtyOrdered(existing.get().getQtyOrdered() + quantity);
            lineRepository.save(existing.get());
        } else {
            lineRepository.save(new CustomerOrderLineEntity(0, orderId, productId, quantity, 0, 0, 0));
        }
    }

    @Override
    @Transactional
    public void removeLine(int orderLineId) {
        CustomerOrderLineEntity line = lineRepository.findById(orderLineId)
                .orElseThrow(() -> new NotFoundException("Строка заказа " + orderLineId + " не найдена"));
        requireStatus(getOrderEntity(line.getOrderId()), "Удалять строки", OrderStatus.NEW);
        lineRepository.delete(line);
    }

    // ---------- резервирование ----------

    @Override
    @Transactional
    public ReserveResult reserve(int orderId) {
        CustomerOrderEntity order = getOrderEntity(orderId);
        OrderStatus status = OrderStatus.fromCode(order.getStatusId());
        if (!status.isEditable()) {
            throw new WmsException("Резервировать можно только заказ в статусе «Новый» или «Частично зарезервирован», "
                    + "текущий статус: «" + status.getTitle() + "»");
        }
        List<CustomerOrderLineEntity> lines = lineRepository.findByOrderIdOrderByLineIdAsc(orderId);
        if (lines.isEmpty()) {
            throw new WmsException("В заказе нет строк");
        }

        List<Integer> excluded = List.of(
                wmsProperties.getLocations().getReceivingId(), wmsProperties.getLocations().getShippingId());
        int reservedNow = 0;
        int shortage = 0;
        for (CustomerOrderLineEntity line : lines) {
            int need = line.getQtyOrdered() - line.getQtyReserved();
            for (AvailableStockRow stock : reservationRepository.findAvailableFefo(line.getProductId(), excluded)) {
                if (need <= 0) break;
                int available = stock.getAvailable() == null ? 0 : stock.getAvailable();
                if (available <= 0) continue;
                int take = Math.min(need, available);
                reservationRepository.save(new StockReservationEntity(0, orderId, line.getLineId(),
                        line.getProductId(), stock.getBatchId(), stock.getLocationId(), take));
                line.setQtyReserved(line.getQtyReserved() + take);
                need -= take;
                reservedNow += take;
            }
            lineRepository.save(line);
            shortage += Math.max(need, 0);
        }

        OrderStatus newStatus = shortage == 0 ? OrderStatus.RESERVED : OrderStatus.PARTIALLY_RESERVED;
        order.setStatusId(newStatus.getCode());
        orderRepository.save(order);
        log.info("Резерв по заказу {}: +{} шт., не хватает {} шт., статус «{}»",
                order.getDocumentNumber(), reservedNow, shortage, newStatus.getTitle());
        return new ReserveResult(reservedNow, shortage, newStatus.getTitle());
    }

    @Override
    @Transactional
    public void cancel(int orderId) {
        CustomerOrderEntity order = getOrderEntity(orderId);
        OrderStatus status = OrderStatus.fromCode(order.getStatusId());
        if (!status.isEditable()) {
            throw new WmsException("Отменить можно только заказ, не переданный в сборку; текущий статус: «"
                    + status.getTitle() + "»");
        }
        reservationRepository.deleteAll(reservationRepository.findByOrderIdOrderByReservationIdAsc(orderId));
        for (CustomerOrderLineEntity line : lineRepository.findByOrderIdOrderByLineIdAsc(orderId)) {
            line.setQtyReserved(0);
            lineRepository.save(line);
        }
        order.setStatusId(OrderStatus.CANCELLED.getCode());
        orderRepository.save(order);
        log.info("Отменён заказ id={} ({})", orderId, order.getDocumentNumber());
    }

    // ---------- представления ----------

    @Override
    @Transactional(readOnly = true)
    public List<OrderView> getOrders(Integer statusCode) {
        List<OrderView> result = new ArrayList<>();
        for (CustomerOrderEntity order : orderRepository.findAllByOrderByOrderIdDesc()) {
            if (Boolean.TRUE.equals(order.getDel())) continue;
            if (statusCode != null && order.getStatusId() != statusCode) continue;
            result.add(toView(order, false));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderView getOrder(int orderId) {
        return toView(getOrderEntity(orderId), true);
    }

    private OrderView toView(CustomerOrderEntity order, boolean withReservations) {
        SimpleDateFormat dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
        List<StockReservationEntity> reservations = withReservations
                ? reservationRepository.findByOrderIdOrderByReservationIdAsc(order.getOrderId())
                : List.of();

        List<LineView> lines = new ArrayList<>();
        int ordered = 0;
        int reserved = 0;
        for (CustomerOrderLineEntity line : lineRepository.findByOrderIdOrderByLineIdAsc(order.getOrderId())) {
            Optional<ProductEntity> product = productRepository.findById(line.getProductId());
            List<ReservationView> lineReservations = new ArrayList<>();
            for (StockReservationEntity r : reservations) {
                if (r.getOrderLineId() != line.getLineId()) continue;
                String expiry = batchRepository.findById(r.getBatchId())
                        .map(BatchEntity::getExpiry_date)
                        .map(date::format)
                        .orElse("—");
                lineReservations.add(new ReservationView(
                        locationRepository.findById(r.getLocationId()).map(LocationEntity::getLocationCode).orElse("?"),
                        r.getBatchId(), expiry, r.getQuantity()));
            }
            lines.add(new LineView(line.getLineId(),
                    product.map(ProductEntity::getProductCode).orElse("?"),
                    product.map(ProductEntity::getProductName).orElse("товар " + line.getProductId()),
                    line.getQtyOrdered(), line.getQtyReserved(), line.getQtyPicked(), line.getQtyShipped(),
                    lineReservations));
            ordered += line.getQtyOrdered();
            reserved += line.getQtyReserved();
        }
        OrderStatus status = OrderStatus.fromCode(order.getStatusId());
        String customer = customerRepository.findById(order.getCustomerId())
                .map(c -> c.getCustomerName() + " (" + c.getCustomerCode() + ")")
                .orElse("клиент " + order.getCustomerId());
        return new OrderView(order.getOrderId(), order.getDocumentNumber(), customer,
                order.getOrderDate() != null ? dateTime.format(order.getOrderDate()) : "",
                order.getShipDate() != null ? date.format(order.getShipDate()) : "",
                status.getCode(), status.getTitle(), status.isEditable(), order.getNote(),
                ordered, reserved, lines);
    }

    // ---------- вспомогательные ----------

    private void checkUniqueNumber(String number) {
        orderRepository.findFirstByDocumentNumberAndStatusIdNot(number, OrderStatus.CANCELLED.getCode())
                .ifPresent(existing -> {
                    throw new WmsException("Заказ " + number + " уже существует (id " + existing.getOrderId() + ")");
                });
    }

    private CustomerOrderEntity getOrderEntity(int orderId) {
        return orderRepository.findById(orderId)
                .filter(o -> !Boolean.TRUE.equals(o.getDel()))
                .orElseThrow(() -> new NotFoundException("Заказ " + orderId + " не найден"));
    }

    private static void requireStatus(CustomerOrderEntity order, String action, OrderStatus expected) {
        OrderStatus actual = OrderStatus.fromCode(order.getStatusId());
        if (actual != expected) {
            throw new WmsException(action + " можно только в заказе со статусом «" + expected.getTitle()
                    + "», текущий статус: «" + actual.getTitle() + "»");
        }
    }

    static Timestamp parseDate(String value) {
        if (isBlank(value)) return null;
        try {
            return Timestamp.valueOf(LocalDate.parse(value.trim().substring(0, Math.min(10, value.trim().length()))).atStartOfDay());
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
