package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderLineEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ShipmentEntity;
import com.tynyany.simplewmsv2.dao.ShipmentLineEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.OrderStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultShippingServiceTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentLineRepository shipmentLineRepository;
    private CustomerOrderRepository orderRepository;
    private PickingRouteRepository routeRepository;
    private PickingRouteLineRepository routeLineRepository;
    private ProductRepository productRepository;
    private InventoryService inventoryService;
    private DefaultShippingService service;

    private final List<ShipmentLineEntity> shipmentLines = new ArrayList<>();
    private final List<PickingRouteLineEntity> routeLines = new ArrayList<>();
    private CustomerOrderEntity order;
    private CustomerOrderLineEntity line1;
    private CustomerOrderLineEntity line2;

    @BeforeEach
    void setUp() {
        shipmentRepository = mock(ShipmentRepository.class);
        shipmentLineRepository = mock(ShipmentLineRepository.class);
        orderRepository = mock(CustomerOrderRepository.class);
        CustomerOrderLineRepository orderLineRepository = mock(CustomerOrderLineRepository.class);
        routeRepository = mock(PickingRouteRepository.class);
        routeLineRepository = mock(PickingRouteLineRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryService = mock(InventoryService.class);
        WmsProperties properties = new WmsProperties();
        properties.getLocations().setShippingId(2);
        service = new DefaultShippingService(shipmentRepository, shipmentLineRepository, orderRepository,
                orderLineRepository, routeRepository, routeLineRepository, customerRepository, productRepository,
                mock(EmployeeRepository.class), inventoryService, properties);

        order = new CustomerOrderEntity();
        order.setOrderId(10);
        order.setDocumentNumber("ПР-1");
        order.setCustomerId(1);
        order.setStatusId(OrderStatus.PICKED.getCode());
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1);
        customer.setCustomerCode("К000001");
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

        line1 = new CustomerOrderLineEntity(100, 10, 5, 50, 50, 50, 0);
        line2 = new CustomerOrderLineEntity(101, 10, 2, 20, 18, 18, 0);
        when(orderLineRepository.findById(100)).thenReturn(Optional.of(line1));
        when(orderLineRepository.findById(101)).thenReturn(Optional.of(line2));

        PickingRouteEntity route = new PickingRouteEntity();
        route.setRouteId(7);
        when(routeRepository.findByOrderAndStatuses(eq(10), any())).thenReturn(List.of(route));
        routeLines.add(routeLine(100, 5, 1, 10));
        routeLines.add(routeLine(100, 5, 2, 40));
        routeLines.add(routeLine(101, 2, 3, 18));
        when(routeLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(7)).thenAnswer(inv -> routeLines);

        when(shipmentRepository.findActiveByOrderId(anyInt())).thenReturn(Optional.empty());
        when(shipmentRepository.save(any())).thenAnswer(inv -> {
            ShipmentEntity s = inv.getArgument(0);
            if (s.getShipmentId() == 0) s.setShipmentId(3);
            when(shipmentRepository.findById(3)).thenReturn(Optional.of(s));
            return s;
        });
        when(shipmentLineRepository.save(any())).thenAnswer(inv -> {
            ShipmentLineEntity l = inv.getArgument(0);
            shipmentLines.add(l);
            return l;
        });
        when(shipmentLineRepository.findByShipmentIdOrderByLineIdAsc(3)).thenAnswer(inv -> shipmentLines);
        product(5, "074519");
        product(2, "029952");
    }

    private PickingRouteLineEntity routeLine(int orderLineId, int productId, int batchId, int picked) {
        PickingRouteLineEntity l = new PickingRouteLineEntity();
        l.setOrderLineId(orderLineId);
        l.setProductId(productId);
        l.setBatchId(batchId);
        l.setQtyPlanned(picked);
        l.setQtyPicked(picked);
        l.setComplete(true);
        return l;
    }

    private void product(int id, String code) {
        ProductEntity p = new ProductEntity();
        p.setProductID(id);
        p.setProductCode(code);
        p.setWeight(0.1F);
        when(productRepository.findById(id)).thenReturn(Optional.of(p));
    }

    @Test
    void shipWritesOffPickedQuantitiesAndMarksOrderShipped() {
        int id = service.ship(10, 3);

        assertThat(id).isEqualTo(3);
        verify(inventoryService).writeOff(1, 2, 10, 3, MovementType.SHIPPING);
        verify(inventoryService).writeOff(2, 2, 40, 3, MovementType.SHIPPING);
        verify(inventoryService).writeOff(3, 2, 18, 3, MovementType.SHIPPING);
        assertThat(shipmentLines).hasSize(3);
        assertThat(line1.getQtyShipped()).isEqualTo(50);
        assertThat(line2.getQtyShipped()).isEqualTo(18);
        assertThat(order.getStatusId()).isEqualTo(OrderStatus.SHIPPED.getCode());
        verify(shipmentRepository, times(2)).save(any());
    }

    @Test
    void documentNumberIsAssignedFromId() {
        service.ship(10, 3);
        assertThat(shipmentRepository.findById(3).orElseThrow().getDocumentNumber()).isEqualTo("ОТГ-3");
    }

    @Test
    void notPickedOrderIsRejected() {
        order.setStatusId(OrderStatus.PICKING.getCode());
        assertThatThrownBy(() -> service.ship(10, 3))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("только собранный");
        verify(inventoryService, never()).writeOff(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void secondShipmentIsRejected() {
        when(shipmentRepository.findActiveByOrderId(10)).thenReturn(Optional.of(new ShipmentEntity()));
        assertThatThrownBy(() -> service.ship(10, 3))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("уже отгружен");
    }

    @Test
    void nothingPickedIsRejected() {
        routeLines.forEach(l -> l.setQtyPicked(0));
        assertThatThrownBy(() -> service.ship(10, 3))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("ничего не собрано");
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void erpExportGroupsLinesByProductCode() {
        service.ship(10, 3);
        ShipmentEntity shipped = shipmentRepository.findById(3).orElseThrow();
        when(shipmentRepository.findShippedSince(any(Timestamp.class))).thenReturn(List.of(shipped));

        List<ShippingService.ErpShipment> result = service.getForErp(LocalDate.now());

        assertThat(result).hasSize(1);
        ShippingService.ErpShipment shipment = result.get(0);
        assertThat(shipment.number()).isEqualTo("ОТГ-3");
        assertThat(shipment.orderERP()).isEqualTo("ПР-1");
        assertThat(shipment.customerCode()).isEqualTo("К000001");
        assertThat(shipment.confirmed()).isFalse();
        assertThat(shipment.body()).containsExactly(
                new ShippingService.ErpShipmentLine("074519", 50),
                new ShippingService.ErpShipmentLine("029952", 18));
    }

    @Test
    void confirmExportSetsDateOnce() {
        service.ship(10, 3);
        service.confirmExport(3);
        Timestamp first = shipmentRepository.findById(3).orElseThrow().getExportedAt();
        service.confirmExport(3);

        assertThat(first).isNotNull();
        assertThat(shipmentRepository.findById(3).orElseThrow().getExportedAt()).isSameAs(first);
    }
}
