package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderLineEntity;
import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteLineEntity;
import com.tynyany.simplewmsv2.dao.StockReservationEntity;
import com.tynyany.simplewmsv2.dao.ZoneEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.domain.PickingRouteStatus;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderLineRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteLineRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import com.tynyany.simplewmsv2.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultPickingServiceTest {

    private PickingRouteRepository routeRepository;
    private PickingRouteLineRepository routeLineRepository;
    private CustomerOrderRepository orderRepository;
    private CustomerOrderLineRepository orderLineRepository;
    private StockReservationRepository reservationRepository;
    private LocationRepository locationRepository;
    private ZoneRepository zoneRepository;
    private InventoryService inventoryService;
    private DefaultPickingService service;

    private final List<PickingRouteEntity> routes = new ArrayList<>();
    private final List<PickingRouteLineEntity> routeLines = new ArrayList<>();
    private CustomerOrderEntity order;
    private CustomerOrderLineEntity orderLine;

    @BeforeEach
    void setUp() {
        routeRepository = mock(PickingRouteRepository.class);
        routeLineRepository = mock(PickingRouteLineRepository.class);
        orderRepository = mock(CustomerOrderRepository.class);
        orderLineRepository = mock(CustomerOrderLineRepository.class);
        reservationRepository = mock(StockReservationRepository.class);
        locationRepository = mock(LocationRepository.class);
        zoneRepository = mock(ZoneRepository.class);
        inventoryService = mock(InventoryService.class);
        WmsProperties properties = new WmsProperties();
        properties.getLocations().setReceivingId(1);
        properties.getLocations().setShippingId(2);
        service = new DefaultPickingService(routeRepository, routeLineRepository, orderRepository, orderLineRepository,
                reservationRepository, mock(CustomerRepository.class), mock(ProductRepository.class), locationRepository,
                zoneRepository, mock(BatchRepository.class), mock(EmployeeRepository.class), inventoryService, properties);

        order = new CustomerOrderEntity();
        order.setOrderId(10);
        order.setDocumentNumber("WMS-10");
        order.setStatusId(OrderStatus.RESERVED.getCode());
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        orderLine = new CustomerOrderLineEntity(100, 10, 7, 15, 15, 0, 0);
        when(orderLineRepository.findById(100)).thenReturn(Optional.of(orderLine));

        when(routeRepository.save(any())).thenAnswer(inv -> {
            PickingRouteEntity r = inv.getArgument(0);
            if (r.getRouteId() == 0) {
                r.setRouteId(50 + routes.size());
                routes.add(r);
            }
            when(routeRepository.findById(r.getRouteId())).thenReturn(Optional.of(r));
            return r;
        });
        when(routeRepository.findByOrderAndStatuses(anyInt(), any())).thenReturn(List.of());
        when(routeLineRepository.save(any())).thenAnswer(inv -> {
            PickingRouteLineEntity l = inv.getArgument(0);
            if (l.getLineId() == 0) {
                l.setLineId(500 + routeLines.size());
                routeLines.add(l);
            }
            when(routeLineRepository.findById(l.getLineId())).thenReturn(Optional.of(l));
            return l;
        });
        when(routeLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(anyInt()))
                .thenAnswer(inv -> new ArrayList<>(routeLines));

        when(zoneRepository.findAll()).thenReturn(List.of(new ZoneEntity(3, "Секция 1", "", "SC1", false)));
        location(101, "SC1-01-01-1-1", "01", 1, 1);
        location(112, "SC1-02-03-2-1", "02", 3, 2);
        location(104, "SC1-01-02-2-1", "01", 2, 2);
    }

    private void location(int id, String code, String row, int x, int y) {
        LocationEntity l = new LocationEntity();
        l.setLocationID(id);
        l.setLocationCode(code);
        l.setRow(row);
        l.setX(x);
        l.setY(y);
        l.setZ(1);
        l.setZoneID(3);
        when(locationRepository.findById(id)).thenReturn(Optional.of(l));
    }

    private void reservations(StockReservationEntity... list) {
        when(reservationRepository.findByOrderIdOrderByReservationIdAsc(10)).thenReturn(List.of(list));
        for (StockReservationEntity r : list) {
            when(reservationRepository.findById(r.getReservationId())).thenReturn(Optional.of(r));
        }
    }

    @Test
    void createRouteSortsLinesByWalkAndMovesOrderToPicking() {
        reservations(
                new StockReservationEntity(1, 10, 100, 7, 1, 112, 5),
                new StockReservationEntity(2, 10, 100, 7, 2, 101, 4),
                new StockReservationEntity(3, 10, 100, 7, 3, 104, 6));

        int routeId = service.createRoute(10);

        assertThat(routeId).isEqualTo(50);
        assertThat(routeLines).extracting(PickingRouteLineEntity::getLocationId).containsExactly(101, 104, 112);
        assertThat(routeLines).extracting(PickingRouteLineEntity::getSortOrder).containsExactly(1, 2, 3);
        assertThat(routeLines).extracting(PickingRouteLineEntity::getQtyPlanned).containsExactly(4, 6, 5);
        assertThat(order.getStatusId()).isEqualTo(OrderStatus.PICKING.getCode());
        assertThat(routes.get(0).getStatusId()).isEqualTo(PickingRouteStatus.CREATED.getCode());
    }

    @Test
    void createRouteRejectsPartiallyReservedOrder() {
        order.setStatusId(OrderStatus.PARTIALLY_RESERVED.getCode());
        assertThatThrownBy(() -> service.createRoute(10))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("полностью зарезервированный");
        verify(routeRepository, never()).save(any());
    }

    @Test
    void createRouteRejectsOrderAlreadyInPicking() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 15));
        when(routeRepository.findByOrderAndStatuses(eq(10), any())).thenReturn(List.of(new PickingRouteEntity()));
        assertThatThrownBy(() -> service.createRoute(10))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("уже передан в сборку");
    }

    @Test
    void confirmBeforeStartIsRejected() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 15));
        service.createRoute(10);
        assertThatThrownBy(() -> service.confirmLine(500, 15, 3))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("В работе");
        verify(inventoryService, never()).move(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void confirmAllLinesMovesStockToShippingAndCompletesOrder() {
        StockReservationEntity r1 = new StockReservationEntity(1, 10, 100, 7, 1, 101, 9);
        StockReservationEntity r2 = new StockReservationEntity(2, 10, 100, 7, 2, 104, 6);
        reservations(r1, r2);
        int routeId = service.createRoute(10);
        service.startRoute(routeId, 3);

        assertThat(service.confirmLine(500, 9, 3)).isFalse();
        assertThat(service.confirmLine(501, 6, 3)).isTrue();

        verify(inventoryService).move(1, 7, 101, 2, 9, 3, MovementType.PICKING);
        verify(inventoryService).move(2, 7, 104, 2, 6, 3, MovementType.PICKING);
        verify(reservationRepository).delete(r1);
        verify(reservationRepository).delete(r2);
        assertThat(orderLine.getQtyPicked()).isEqualTo(15);
        assertThat(orderLine.getQtyReserved()).isEqualTo(15);
        assertThat(routes.get(0).getStatusId()).isEqualTo(PickingRouteStatus.COMPLETED.getCode());
        assertThat(order.getStatusId()).isEqualTo(OrderStatus.PICKED.getCode());
    }

    @Test
    void shortageIsRecordedWithoutMovingMissingQuantity() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 15));
        int routeId = service.createRoute(10);
        service.startRoute(routeId, 3);

        service.confirmLine(500, 12, 3);

        verify(inventoryService).move(1, 7, 101, 2, 12, 3, MovementType.PICKING);
        assertThat(orderLine.getQtyPicked()).isEqualTo(12);
        assertThat(orderLine.getQtyReserved()).isEqualTo(12);
        assertThat(routeLines.get(0).getQtyPicked()).isEqualTo(12);
    }

    @Test
    void zeroPickedDoesNotTouchStock() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 15));
        int routeId = service.createRoute(10);
        service.startRoute(routeId, 3);

        service.confirmLine(500, 0, 3);

        verify(inventoryService, never()).move(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        assertThat(orderLine.getQtyReserved()).isZero();
    }

    @Test
    void quantityAbovePlanIsRejected() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 15));
        int routeId = service.createRoute(10);
        service.startRoute(routeId, 3);
        assertThatThrownBy(() -> service.confirmLine(500, 16, 3))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("от 0 до 15");
    }

    @Test
    void cancelReturnsOrderToReserved() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 15));
        int routeId = service.createRoute(10);

        service.cancelRoute(routeId);

        assertThat(routes.get(0).getStatusId()).isEqualTo(PickingRouteStatus.CANCELLED.getCode());
        assertThat(order.getStatusId()).isEqualTo(OrderStatus.RESERVED.getCode());
    }

    @Test
    void cancelAfterConfirmedLineIsRejected() {
        reservations(new StockReservationEntity(1, 10, 100, 7, 1, 101, 5),
                new StockReservationEntity(2, 10, 100, 7, 2, 104, 10));
        int routeId = service.createRoute(10);
        service.startRoute(routeId, 3);
        service.confirmLine(500, 5, 3);

        assertThatThrownBy(() -> service.cancelRoute(routeId))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("уже собрана");
    }
}
