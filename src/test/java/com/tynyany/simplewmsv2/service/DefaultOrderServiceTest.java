package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.StockReservationEntity;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.dto.AvailableStockRow;
import com.tynyany.simplewmsv2.entity.OrderJSON;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderLineRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
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

class DefaultOrderServiceTest {

    private CustomerOrderRepository orderRepository;
    private CustomerOrderLineRepository lineRepository;
    private StockReservationRepository reservationRepository;
    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private DefaultOrderService service;

    private final List<CustomerOrderLineEntity> lines = new ArrayList<>();
    private final List<StockReservationEntity> reservations = new ArrayList<>();

    @BeforeEach
    void setUp() {
        orderRepository = mock(CustomerOrderRepository.class);
        lineRepository = mock(CustomerOrderLineRepository.class);
        reservationRepository = mock(StockReservationRepository.class);
        customerRepository = mock(CustomerRepository.class);
        productRepository = mock(ProductRepository.class);
        WmsProperties properties = new WmsProperties();
        properties.getLocations().setReceivingId(1);
        properties.getLocations().setShippingId(2);
        service = new DefaultOrderService(orderRepository, lineRepository, reservationRepository, customerRepository,
                productRepository, mock(LocationRepository.class), mock(BatchRepository.class), properties);

        when(orderRepository.save(any())).thenAnswer(inv -> {
            CustomerOrderEntity o = inv.getArgument(0);
            if (o.getOrderId() == 0) o.setOrderId(10);
            when(orderRepository.findById(o.getOrderId())).thenReturn(Optional.of(o));
            return o;
        });
        when(lineRepository.save(any())).thenAnswer(inv -> {
            CustomerOrderLineEntity l = inv.getArgument(0);
            if (l.getLineId() == 0) {
                l.setLineId(100 + lines.size());
                lines.add(l);
            }
            return l;
        });
        when(lineRepository.findByOrderIdOrderByLineIdAsc(anyInt())).thenAnswer(inv -> new ArrayList<>(lines));
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            reservations.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        when(reservationRepository.findByOrderIdOrderByReservationIdAsc(anyInt())).thenAnswer(inv -> new ArrayList<>(reservations));

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1);
        customer.setCustomerCode("К000001");
        when(customerRepository.findByCustomerCode("К000001")).thenReturn(Optional.of(customer));
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(productRepository.findOneByProductCode("029952")).thenReturn(Optional.of(product(5)));
        when(productRepository.findOneByProductCode("074519")).thenReturn(Optional.of(product(6)));
        when(productRepository.findById(5)).thenReturn(Optional.of(product(5)));
    }

    @Test
    void erpOrderIsCreatedAndReservedByFefoOrder() {
        // FEFO-порядок задаёт запрос: сначала ближайший срок
        when(reservationRepository.findAvailableFefo(eq(5), any())).thenReturn(List.of(
                stock(77, 101, 30, "2027-01-31"),
                stock(78, 102, 100, "2027-06-30")));
        when(reservationRepository.findAvailableFefo(eq(6), any())).thenReturn(List.of(stock(80, 103, 10, null)));

        OrderService.ErpOrderResult result = service.createFromErp(json("ПР-1", line("029952", "50"), line("074519", "10")));

        assertThat(result.orderId()).isEqualTo(10);
        assertThat(result.linesCreated()).isEqualTo(2);
        assertThat(result.reserve().shortage()).isZero();
        assertThat(result.reserve().status()).isEqualTo(OrderStatus.RESERVED.getTitle());
        assertThat(reservations).extracting(StockReservationEntity::getBatchId).containsExactly(77, 78, 80);
        assertThat(reservations).extracting(StockReservationEntity::getQuantity).containsExactly(30, 20, 10);
        assertThat(lines.get(0).getQtyReserved()).isEqualTo(50);
    }

    @Test
    void shortageGivesPartialReservationAndRepeatTakesTheRest() {
        when(reservationRepository.findAvailableFefo(eq(5), any())).thenReturn(List.of(stock(77, 101, 30, null)));
        OrderService.ErpOrderResult result = service.createFromErp(json("ПР-2", line("029952", "50")));

        assertThat(result.reserve().shortage()).isEqualTo(20);
        assertThat(result.reserve().status()).isEqualTo(OrderStatus.PARTIALLY_RESERVED.getTitle());

        // поступил товар: дорезервирование берёт только недостающее
        when(reservationRepository.findAvailableFefo(eq(5), any())).thenReturn(List.of(stock(90, 104, 100, null)));
        OrderService.ReserveResult again = service.reserve(10);

        assertThat(again.reservedNow()).isEqualTo(20);
        assertThat(again.shortage()).isZero();
        assertThat(reservations).extracting(StockReservationEntity::getQuantity).containsExactly(30, 20);
    }

    @Test
    void systemCellsAreExcludedFromReservation() {
        when(reservationRepository.findAvailableFefo(eq(5), any())).thenReturn(List.of());
        service.createFromErp(json("ПР-3", line("029952", "1")));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Collection<Integer>> excluded = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(reservationRepository).findAvailableFefo(eq(5), excluded.capture());
        assertThat(excluded.getValue()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void duplicateNumberAndUnknownCustomerAreRejected() {
        when(orderRepository.findFirstByDocumentNumberAndStatusIdNot("ПР-1", OrderStatus.CANCELLED.getCode()))
                .thenReturn(Optional.of(new CustomerOrderEntity(5, "ПР-1", 1, null, null, 0, null, null, false)));
        assertThatThrownBy(() -> service.createFromErp(json("ПР-1", line("029952", "1"))))
                .hasMessageContaining("уже существует (id 5)");

        OrderJSON unknown = json("ПР-9", line("029952", "1"));
        unknown.header.customerCode = "НЕТ";
        assertThatThrownBy(() -> service.createFromErp(unknown)).isInstanceOf(NotFoundException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelReleasesReservations() {
        when(reservationRepository.findAvailableFefo(eq(5), any())).thenReturn(List.of(stock(77, 101, 100, null)));
        service.createFromErp(json("ПР-4", line("029952", "10")));
        CustomerOrderEntity order = orderRepository.findById(10).orElseThrow();

        service.cancel(10);

        verify(reservationRepository).deleteAll(any());
        assertThat(lines.get(0).getQtyReserved()).isZero();
        assertThat(order.getStatusId()).isEqualTo(OrderStatus.CANCELLED.getCode());
        assertThatThrownBy(() -> service.reserve(10)).isInstanceOf(WmsException.class);
        assertThatThrownBy(() -> service.cancel(10)).isInstanceOf(WmsException.class);
    }

    @Test
    void manualOrderGetsNumberAndLinesOnlyWhileNew() {
        int id = service.create(1, "", "2026-10-20", "срочно");
        CustomerOrderEntity order = orderRepository.findById(id).orElseThrow();
        assertThat(order.getDocumentNumber()).isEqualTo("WMS-10");
        assertThat(order.getShipDate()).isEqualTo(Timestamp.valueOf("2026-10-20 00:00:00"));

        service.addLine(id, 5, 3);
        service.addLine(id, 5, 2);   // тот же товар — количество суммируется
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getQtyOrdered()).isEqualTo(5);
        assertThatThrownBy(() -> service.addLine(id, 5, 0)).hasMessageContaining("больше нуля");

        when(reservationRepository.findAvailableFefo(eq(5), any())).thenReturn(List.of(stock(77, 101, 100, null)));
        service.reserve(id);
        assertThatThrownBy(() -> service.addLine(id, 5, 1)).hasMessageContaining("«Новый»");
        verify(lineRepository, times(0)).delete(any());
    }

    @Test
    void wrongShipDateIsRejected() {
        assertThatThrownBy(() -> service.create(1, "", "20.10.2026", null)).hasMessageContaining("формат даты");
    }

    // ---------- вспомогательные ----------

    private static ProductEntity product(int id) {
        ProductEntity p = new ProductEntity();
        p.setProductID(id);
        p.setProductCode("code" + id);
        p.setProductName("Товар " + id);
        return p;
    }

    private static AvailableStockRow stock(int batchId, int locationId, int available, String expiry) {
        return new AvailableStockRow() {
            public Integer getBatchId() { return batchId; }
            public Integer getLocationId() { return locationId; }
            public Integer getAvailable() { return available; }
            public Timestamp getExpiryDate() { return expiry == null ? null : Timestamp.valueOf(expiry + " 00:00:00"); }
        };
    }

    private static OrderJSON.Line line(String code, String qnt) {
        OrderJSON.Line l = new OrderJSON.Line();
        l.code = code;
        l.qnt = qnt;
        return l;
    }

    private static OrderJSON json(String number, OrderJSON.Line... body) {
        OrderJSON json = new OrderJSON();
        json.header = new OrderJSON.Header();
        json.header.orderERP = number;
        json.header.customerCode = "К000001";
        json.header.orderDate = "2026-10-10 09:00:00";
        json.header.shipDate = "2026-10-11";
        json.body = new ArrayList<>(List.of(body));
        return json;
    }
}
