package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ShipmentEntity;
import com.tynyany.simplewmsv2.dao.ShipmentLineEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.domain.PickingRouteStatus;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.CustomerOrderLineRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteLineRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.ShipmentLineRepository;
import com.tynyany.simplewmsv2.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultShippingService implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentLineRepository shipmentLineRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderLineRepository orderLineRepository;
    private final PickingRouteRepository pickingRouteRepository;
    private final PickingRouteLineRepository pickingRouteLineRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final InventoryService inventoryService;
    private final WmsProperties wmsProperties;

    /** Ключ строки отгрузки: строка заказа × партия. */
    private record LineKey(int orderLineId, int productId, int batchId) {}

    @Override
    @Transactional
    public int ship(int orderId, int employeeId) {
        CustomerOrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !Boolean.TRUE.equals(o.getDel()))
                .orElseThrow(() -> new NotFoundException("Заказ " + orderId + " не найден"));
        OrderStatus status = OrderStatus.fromCode(order.getStatusId());
        if (status != OrderStatus.PICKED) {
            throw new WmsException("Отгрузить можно только собранный заказ, текущий статус: «" + status.getTitle() + "»");
        }
        if (shipmentRepository.findActiveByOrderId(orderId).isPresent()) {
            throw new WmsException("Заказ " + order.getDocumentNumber() + " уже отгружен");
        }
        PickingRouteEntity route = pickingRouteRepository
                .findByOrderAndStatuses(orderId, List.of(PickingRouteStatus.COMPLETED.getCode()))
                .stream().findFirst()
                .orElseThrow(() -> new WmsException("Для заказа " + order.getDocumentNumber() + " нет завершённого маршрута сборки"));

        Map<LineKey, Integer> quantities = new LinkedHashMap<>();
        for (PickingRouteLineEntity line : pickingRouteLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(route.getRouteId())) {
            if (line.getQtyPicked() > 0) {
                quantities.merge(new LineKey(line.getOrderLineId(), line.getProductId(), line.getBatchId()),
                        line.getQtyPicked(), Integer::sum);
            }
        }
        if (quantities.isEmpty()) {
            throw new WmsException("По заказу " + order.getDocumentNumber() + " ничего не собрано — отгружать нечего");
        }

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setOrderId(orderId);
        shipment.setEmployeeId(employeeId);
        shipment.setShippedAt(now());
        shipment.setDel(false);
        shipment = shipmentRepository.save(shipment);
        shipment.setDocumentNumber("ОТГ-" + shipment.getShipmentId());
        shipmentRepository.save(shipment);

        int shippingLocationId = wmsProperties.getLocations().getShippingId();
        int total = 0;
        for (Map.Entry<LineKey, Integer> entry : quantities.entrySet()) {
            LineKey key = entry.getKey();
            int quantity = entry.getValue();
            inventoryService.writeOff(key.batchId(), shippingLocationId, quantity, employeeId, MovementType.SHIPPING);
            shipmentLineRepository.save(new ShipmentLineEntity(0, shipment.getShipmentId(), key.orderLineId(),
                    key.productId(), key.batchId(), quantity));
            orderLineRepository.findById(key.orderLineId()).ifPresent(orderLine -> {
                orderLine.setQtyShipped(orderLine.getQtyShipped() + quantity);
                orderLineRepository.save(orderLine);
            });
            total += quantity;
        }

        order.setStatusId(OrderStatus.SHIPPED.getCode());
        orderRepository.save(order);
        log.info("Отгрузка {} по заказу {}: строк {}, {} шт.", shipment.getDocumentNumber(),
                order.getDocumentNumber(), quantities.size(), total);
        return shipment.getShipmentId();
    }

    @Override
    @Transactional
    public void confirmExport(int shipmentId) {
        ShipmentEntity shipment = getShipmentEntity(shipmentId);
        if (shipment.getExportedAt() == null) {
            shipment.setExportedAt(now());
            shipmentRepository.save(shipment);
            log.info("ERP подтвердила получение отгрузки {}", shipment.getDocumentNumber());
        }
    }

    // ---------- представления ----------

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentView> getShipments() {
        List<ShipmentView> result = new ArrayList<>();
        for (ShipmentEntity shipment : shipmentRepository.findActiveOrderByIdDesc()) {
            result.add(toView(shipment));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentView getShipment(int shipmentId) {
        return toView(getShipmentEntity(shipmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findShipmentIdByOrder(int orderId) {
        return shipmentRepository.findActiveByOrderId(orderId).map(ShipmentEntity::getShipmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErpShipment> getForErp(LocalDate from) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ErpShipment> result = new ArrayList<>();
        for (ShipmentEntity shipment : shipmentRepository.findShippedSince(Timestamp.valueOf(from.atStartOfDay()))) {
            Map<String, Integer> byCode = new LinkedHashMap<>();
            for (ShipmentLineEntity line : shipmentLineRepository.findByShipmentIdOrderByLineIdAsc(shipment.getShipmentId())) {
                String code = productRepository.findById(line.getProductId())
                        .map(ProductEntity::getProductCode).orElse(String.valueOf(line.getProductId()));
                byCode.merge(code, line.getQuantity(), Integer::sum);
            }
            List<ErpShipmentLine> body = new ArrayList<>();
            byCode.forEach((code, qnt) -> body.add(new ErpShipmentLine(code, qnt)));
            Optional<CustomerOrderEntity> order = orderRepository.findById(shipment.getOrderId());
            result.add(new ErpShipment(shipment.getShipmentId(), shipment.getDocumentNumber(),
                    order.map(CustomerOrderEntity::getDocumentNumber).orElse(""),
                    order.flatMap(o -> customerRepository.findById(o.getCustomerId()))
                            .map(CustomerEntity::getCustomerCode).orElse(""),
                    shipment.getShippedAt() != null ? format.format(shipment.getShippedAt()) : "",
                    shipment.getExportedAt() != null,
                    body));
        }
        return result;
    }

    private ShipmentView toView(ShipmentEntity shipment) {
        SimpleDateFormat dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Optional<CustomerOrderEntity> order = orderRepository.findById(shipment.getOrderId());
        Optional<CustomerEntity> customer = order.flatMap(o -> customerRepository.findById(o.getCustomerId()));

        List<LineView> lines = new ArrayList<>();
        int total = 0;
        double weight = 0;
        for (ShipmentLineEntity line : shipmentLineRepository.findByShipmentIdOrderByLineIdAsc(shipment.getShipmentId())) {
            Optional<ProductEntity> product = productRepository.findById(line.getProductId());
            double lineWeight = Math.round(line.getQuantity()
                    * product.map(ProductEntity::getWeight).map(Float::doubleValue).orElse(0.0) * 100) / 100.0;
            lines.add(new LineView(product.map(ProductEntity::getProductCode).orElse("?"),
                    product.map(ProductEntity::getProductName).orElse("товар " + line.getProductId()),
                    line.getBatchId(), line.getQuantity(), lineWeight));
            total += line.getQuantity();
            weight += lineWeight;
        }
        String employee = shipment.getEmployeeId() > 0
                ? employeeRepository.findById(shipment.getEmployeeId()).map(e -> e.getEmployeeName()).orElse("")
                : "";
        return new ShipmentView(shipment.getShipmentId(), shipment.getDocumentNumber(), shipment.getOrderId(),
                order.map(CustomerOrderEntity::getDocumentNumber).orElse(""),
                customer.map(CustomerEntity::getCustomerName).orElse(""),
                customer.map(CustomerEntity::getCustomerCode).orElse(""),
                customer.map(CustomerEntity::getAddress).orElse(""),
                shipment.getShippedAt() != null ? dateTime.format(shipment.getShippedAt()) : "",
                employee,
                shipment.getExportedAt() != null ? dateTime.format(shipment.getExportedAt()) : "",
                total, Math.round(weight * 100) / 100.0, lines);
    }

    private ShipmentEntity getShipmentEntity(int shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .filter(s -> !Boolean.TRUE.equals(s.getDel()))
                .orElseThrow(() -> new NotFoundException("Отгрузка " + shipmentId + " не найдена"));
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
