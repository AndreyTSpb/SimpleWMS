package com.tynyany.simplewmsv2.it;

import com.jayway.jsonpath.JsonPath;
import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.PickingRouteLineEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.entity.ReceivingLineForm;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.PickingRouteLineRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.StockRepository;
import com.tynyany.simplewmsv2.repository.StockReservationRepository;
import com.tynyany.simplewmsv2.service.PickingService;
import com.tynyany.simplewmsv2.service.PlacementService;
import com.tynyany.simplewmsv2.service.ReceivingProcessService;
import com.tynyany.simplewmsv2.service.ReportService;
import com.tynyany.simplewmsv2.service.ShippingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Сквозной сценарий склада на настоящей базе: справочники и документы приходят из ERP через API,
 * складские операции выполняются сервисами, результаты уходят в ERP через API.
 * На каждом шаге сверяются остатки по зонам, резервы, статусы и журнал движений.
 */
class WarehouseFlowIT extends IntegrationTest {

    private static final String KEY = "test-api-key";
    private static final int STOREKEEPER = 3;
    private static final int MANAGER = 2;

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private WmsProperties properties;
    @Autowired private ReceivingProcessService receivingService;
    @Autowired private ReceivingLineRepository receivingLineRepository;
    @Autowired private PlacementService placementService;
    @Autowired private PickingService pickingService;
    @Autowired private PickingRouteLineRepository pickingRouteLineRepository;
    @Autowired private ShippingService shippingService;
    @Autowired private CustomerOrderRepository orderRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private StockReservationRepository reservationRepository;
    @Autowired private ReportService reportService;

    @Test
    void fullCycleFromErpReceivingToShipmentExport() throws Exception {
        int receivingZone = properties.getLocations().getReceivingId();
        int shippingZone = properties.getLocations().getShippingId();

        // 1. Справочник товаров из ERP: товар со сроком годности (класс A) и без срока (класс C)
        api(put("/api/v1/products"), """
                [{"code": "IT-001", "name": "Клей ПВА 85 г", "supplierCode": "П000019", "category": "Клей",
                  "abc": "A", "weight": 0.1, "volume": 0.5, "expirationRequired": true},
                 {"code": "IT-002", "name": "Тетрадь 48 л.", "supplierCode": "П000019", "category": "Бумажная продукция",
                  "abc": "C", "weight": 0.2, "volume": 0.3, "expirationRequired": false}]""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(2));
        int glue = productId("IT-001");
        int notebook = productId("IT-002");

        // 2. Приходная накладная из ERP
        MvcResult created = api(post("/api/v1/receivings"), """
                {"number": "IT-З-1", "supplierCode": "П000019", "date": "2026-10-10 08:00:00",
                 "lines": [{"code": "IT-001", "quantity": 100}, {"code": "IT-002", "quantity": 50}]}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("В ожидании"))
                .andReturn();
        int receivingId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // 3. Приёмка: клей полностью, тетрадей на 2 меньше
        receivingService.start(receivingId, STOREKEEPER, null);
        List<ReceivingLineEntity> lines = receivingLineRepository.findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(receivingId, false);
        assertThat(lines).hasSize(2);
        receivingService.processLine(acceptLine(lines.get(0), receivingId, 100, "2027-03-01"), STOREKEEPER);
        receivingService.processLine(acceptLine(lines.get(1), receivingId, 48, ""), STOREKEEPER);
        receivingService.complete(receivingId);

        assertThat(quantity(glue, receivingZone)).isEqualTo(100);
        assertThat(quantity(notebook, receivingZone)).isEqualTo(48);
        assertThat(stockRepository.findUnplaced(receivingZone))
                .extracting(r -> r.getProductId()).contains(glue, notebook);
        apiGet("/api/v1/receivings/IT-З-1")
                .andExpect(jsonPath("$.data.status").value("Завершена"))
                .andExpect(jsonPath("$.data.lines[0].received").value(100))
                .andExpect(jsonPath("$.data.lines[0].expiryDate").value("2027-03-01"))
                .andExpect(jsonPath("$.data.lines[1].received").value(48));

        // 4. Размещение: класс A — в секцию SC1, класс C — в SC2
        PlacementService.GenerationResult generated = placementService.generateRoutes();
        assertThat(generated.linesSkipped()).isZero();
        for (PlacementService.RouteView route : placementService.getRoutes()) {
            if (route.statusCode() != 0) continue;
            placementService.startRoute(route.id(), STOREKEEPER);
            for (PlacementService.RouteLineView line : route.lines()) {
                placementService.confirmLine(line.lineId(), 0, STOREKEEPER);
            }
        }
        assertThat(quantity(glue, receivingZone)).isZero();
        assertThat(quantity(notebook, receivingZone)).isZero();
        assertThat(zonesOf(glue)).containsOnly("SC1");
        assertThat(zonesOf(notebook)).containsOnly("SC2");

        // 5. Заказ из ERP: резерв полный; второй заказ — частичный, затем отмена через API
        MvcResult order = api(post("/api/v1/orders"), """
                {"number": "IT-ПР-1", "customerCode": "К000001", "shipDate": "2026-10-11",
                 "lines": [{"code": "IT-001", "quantity": 30}, {"code": "IT-002", "quantity": 40}]}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("Зарезервирован"))
                .andExpect(jsonPath("$.data.shortage").value(0))
                .andReturn();
        int orderId = JsonPath.read(order.getResponse().getContentAsString(), "$.data.id");

        api(post("/api/v1/orders"), """
                {"number": "IT-ПР-2", "customerCode": "К000001",
                 "lines": [{"code": "IT-002", "quantity": 20}]}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("Частично зарезервирован"))
                .andExpect(jsonPath("$.data.shortage").value(12));
        api(post("/api/v1/orders/IT-ПР-2/cancel"), null).andExpect(status().isOk());
        apiGet("/api/v1/orders/IT-ПР-2").andExpect(jsonPath("$.data.status").value("Отменён"));
        assertThat(reservedFor(orderId)).isEqualTo(70);

        // 6. Сборка: тетрадей собрано на 2 меньше плана
        int routeId = pickingService.createRoute(orderId);
        pickingService.startRoute(routeId, STOREKEEPER);
        for (PickingRouteLineEntity line : pickingRouteLineRepository.findByRouteIdOrderBySortOrderAscLineIdAsc(routeId)) {
            int picked = line.getProductId() == notebook ? line.getQtyPlanned() - 2 : line.getQtyPlanned();
            pickingService.confirmLine(line.getLineId(), picked, STOREKEEPER);
        }
        assertThat(orderStatus(orderId)).isEqualTo(OrderStatus.PICKED);
        assertThat(reservedFor(orderId)).isZero();
        assertThat(quantity(glue, shippingZone)).isEqualTo(30);
        assertThat(quantity(notebook, shippingZone)).isEqualTo(38);

        // 7. Отгрузка и выгрузка в ERP
        int shipmentId = shippingService.ship(orderId, MANAGER);
        assertThat(orderStatus(orderId)).isEqualTo(OrderStatus.SHIPPED);
        assertThat(quantity(glue, shippingZone)).isZero();
        assertThat(quantity(notebook, shippingZone)).isZero();
        apiGet("/api/v1/orders/IT-ПР-1")
                .andExpect(jsonPath("$.data.status").value("Отгружен"))
                .andExpect(jsonPath("$.data.lines[0].shipped").value(30))
                .andExpect(jsonPath("$.data.lines[1].shipped").value(38))
                .andExpect(jsonPath("$.data.shipment").value("ОТГ-" + shipmentId));

        String today = LocalDate.now().toString();
        String export = apiGet("/api/v1/shipments?from=" + today).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> exported = JsonPath.read(export, "$.data[?(@.orderERP == 'IT-ПР-1')].body[*]");
        assertThat(exported).extracting(m -> m.get("code") + "=" + m.get("qnt")).containsExactlyInAnyOrder("IT-001=30", "IT-002=38");
        api(post("/api/v1/shipments/" + shipmentId + "/confirm"), null).andExpect(status().isOk());
        String confirmed = apiGet("/api/v1/shipments?from=" + today).andReturn().getResponse().getContentAsString();
        List<Boolean> confirmedFlags = JsonPath.read(confirmed, "$.data[?(@.id == " + shipmentId + ")].confirmed");
        assertThat(confirmedFlags).containsExactly(true);

        // 8. Баланс: принято = осталось на складе + отгружено; недостача сборки осталась в ячейке
        assertThat(totalQuantity(glue)).isEqualTo(70);
        assertThat(totalQuantity(notebook)).isEqualTo(10);
        assertThat(movedQuantity(glue, MovementType.RECEIVING)).isEqualTo(100);
        assertThat(movedQuantity(glue, MovementType.SHIPPING)).isEqualTo(30);
        assertThat(movedQuantity(notebook, MovementType.RECEIVING)).isEqualTo(48);
        assertThat(movedQuantity(notebook, MovementType.PICKING)).isEqualTo(38);
        assertThat(movedQuantity(notebook, MovementType.SHIPPING)).isEqualTo(38);

        // 9. Отчёты видят результат
        ReportService.ProductStockRow glueStock = reportService.stockByProduct(
                new ReportService.StockFilter("IT-001", null, "", false)).get(0);
        assertThat(glueStock.total()).isEqualTo(70);
        assertThat(glueStock.free()).isEqualTo(70);
        assertThat(reportService.shipmentReport(LocalDate.now(), LocalDate.now()).products())
                .anySatisfy(p -> {
                    assertThat(p.productCode()).isEqualTo("IT-002");
                    assertThat(p.shortage()).isEqualTo(2);
                });
        assertThat(reportService.employeeWork(LocalDate.now(), LocalDate.now()))
                .anySatisfy(w -> assertThat(w.shortageQuantity()).isGreaterThanOrEqualTo(2));
    }

    // ---------- вспомогательные ----------

    private org.springframework.test.web.servlet.ResultActions api(MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("X-API-Key", KEY);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request);
    }

    private org.springframework.test.web.servlet.ResultActions apiGet(String url) throws Exception {
        return api(get(url), null).andExpect(status().isOk());
    }

    private static ReceivingLineForm acceptLine(ReceivingLineEntity line, int receivingId, int quantity, String expiry) {
        return ReceivingLineForm.builder()
                .numLine(line.getReceivingLineID())
                .productId(line.getProductID())
                .qntOrder(line.getQuantityProduct())
                .qntFact(quantity)
                .expirationDate(expiry)
                .orderId(receivingId)
                .note("")
                .build();
    }

    private int productId(String code) {
        return jdbc.queryForObject("SELECT productid FROM product WHERE product_code = ?", Integer.class, code);
    }

    private int quantity(int productId, int locationId) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(quantity), 0) FROM stocks WHERE product_id = ? AND location_id = ?",
                Integer.class, productId, locationId);
    }

    private int totalQuantity(int productId) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(quantity), 0) FROM stocks WHERE product_id = ?", Integer.class, productId);
    }

    private List<String> zonesOf(int productId) {
        return jdbc.queryForList("SELECT DISTINCT z.code FROM stocks s JOIN location l ON l.locationid = s.location_id"
                + " JOIN zones z ON z.zone_id = l.zoneid WHERE s.product_id = ? AND s.quantity > 0", String.class, productId);
    }

    private int reservedFor(int orderId) {
        return reservationRepository.findByOrderIdOrderByReservationIdAsc(orderId).stream()
                .mapToInt(r -> r.getQuantity()).sum();
    }

    private OrderStatus orderStatus(int orderId) {
        return OrderStatus.fromCode(orderRepository.findById(orderId).map(CustomerOrderEntity::getStatusId).orElseThrow());
    }

    private int movedQuantity(int productId, MovementType type) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(m.quantity), 0) FROM stock_movements m"
                + " JOIN batches b ON b.batch_id = m.batch_id WHERE b.product_id = ? AND m.movement_type_id = ?",
                Integer.class, productId, type.getCode());
    }
}
