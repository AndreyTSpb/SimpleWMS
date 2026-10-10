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
import com.tynyany.simplewmsv2.entity.ReceivingERP;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultReceivingProcessServiceTest {

    private static final int RECEIVING_ID = 1;
    private static final int LINE_ID = 11;
    private static final int PRODUCT_ID = 5;
    private static final int SUPPLIER_ID = 2;
    private static final int STOREKEEPER_ID = 3;

    private ReceivingRepository receivingRepository;
    private ReceivingLineRepository lineRepository;
    private ProductRepository productRepository;
    private SupplierRepository supplierRepository;
    private BatchRepository batchRepository;
    private LocationRepository locationRepository;
    private InventoryService inventoryService;
    private DefaultReceivingProcessService service;

    @BeforeEach
    void setUp() {
        receivingRepository = mock(ReceivingRepository.class);
        lineRepository = mock(ReceivingLineRepository.class);
        productRepository = mock(ProductRepository.class);
        supplierRepository = mock(SupplierRepository.class);
        batchRepository = mock(BatchRepository.class);
        locationRepository = mock(LocationRepository.class);
        when(locationRepository.findById(101)).thenReturn(Optional.of(
                new LocationEntity(101, "SC1-01-01-1-1", "01", 1, 1, 1, 1f, 1, 0, 3)));
        inventoryService = mock(InventoryService.class);
        WmsProperties properties = new WmsProperties();
        properties.getLocations().setReceivingId(1);
        properties.getLocations().setShippingId(2);
        service = new DefaultReceivingProcessService(receivingRepository, lineRepository, productRepository,
                supplierRepository, batchRepository, locationRepository, inventoryService, properties);

        when(batchRepository.save(any())).thenAnswer(inv -> {
            BatchEntity b = inv.getArgument(0);
            b.setBatch_id(77);
            return b;
        });
        when(receivingRepository.save(any())).thenAnswer(inv -> {
            ReceivingEntity r = inv.getArgument(0);
            if (r.getReceivingID() == 0) r.setReceivingID(RECEIVING_ID);
            return r;
        });
    }

    // ---------- приёмка строки ----------

    @Test
    void processLineCreatesBatchAndReceivesIntoReceivingCell() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity line = givenLine(false);
        givenProduct(false);

        service.processLine(form(100, ""), 0);

        assertThat(line.getComplete()).isTrue();
        assertThat(line.getQuantityReceived()).isEqualTo(100);
        ArgumentCaptor<BatchEntity> batch = ArgumentCaptor.forClass(BatchEntity.class);
        verify(batchRepository).save(batch.capture());
        assertThat(batch.getValue().getSupplier_id()).isEqualTo(SUPPLIER_ID);
        assertThat(batch.getValue().getReceiving_id()).isEqualTo(LINE_ID);
        assertThat(batch.getValue().getExpiry_date()).isNull();
        verify(inventoryService).receive(77, PRODUCT_ID, 1, 100, STOREKEEPER_ID);
    }

    @Test
    void messagesUsePositionInDocumentNotInternalId() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity line = givenLine(false);
        givenProduct(false);
        ReceivingLineEntity first = new ReceivingLineEntity(5, 1, 0, null, RECEIVING_ID, 0, PRODUCT_ID, false, false, false, "");
        when(lineRepository.findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(RECEIVING_ID, false))
                .thenReturn(new ArrayList<>(List.of(first, line)));

        assertThat(service.processLine(form(10, ""), 0)).isEqualTo("строка 2 «Товар 5»");
        assertThatThrownBy(() -> service.processLine(form(10, ""), 0))
                .hasMessage("Строка 2 «Товар 5» уже принята, повторная приёмка не допускается");
    }

    @Test
    void processLineRecordsExecutorWhenGiven() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        givenLine(false);
        givenProduct(false);

        service.processLine(form(5, ""), 4);

        verify(inventoryService).receive(77, PRODUCT_ID, 1, 5, 4);
    }

    @Test
    void processLineRequiresReceivingInProgress() {
        givenReceiving(ReceivingStatus.WAITING);
        givenLine(false);
        givenProduct(false);

        assertThatThrownBy(() -> service.processLine(form(10, ""), 0))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("В процессе");
        verify(inventoryService, never()).receive(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void processLineCannotBeRepeated() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        givenLine(true);
        givenProduct(false);

        assertThatThrownBy(() -> service.processLine(form(10, ""), 0))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("уже принята");
        verify(batchRepository, never()).save(any());
    }

    @Test
    void processLineRequiresExpirationDateWhenProductTracksIt() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        givenLine(false);
        givenProduct(true);

        assertThatThrownBy(() -> service.processLine(form(10, ""), 0))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("срок годности");

        service.processLine(form(10, "2027-05-31"), 0);
        ArgumentCaptor<BatchEntity> batch = ArgumentCaptor.forClass(BatchEntity.class);
        verify(batchRepository).save(batch.capture());
        assertThat(batch.getValue().getExpiry_date().toLocalDateTime().toLocalDate().toString()).isEqualTo("2027-05-31");
    }

    @Test
    void processLineWithZeroQuantityClosesLineWithoutStock() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity line = givenLine(false);
        givenProduct(false);

        service.processLine(form(0, ""), 0);

        assertThat(line.getComplete()).isTrue();
        verify(batchRepository, never()).save(any());
        verify(inventoryService, never()).receive(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void processLineChecksStorageLocationWhenGiven() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        givenLine(false);
        givenProduct(false);

        assertThatThrownBy(() -> service.processLine(form(10, "", 1), 0))      // ячейка приёмки
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("не являются местом хранения");
        assertThatThrownBy(() -> service.processLine(form(10, "", 999), 0))
                .isInstanceOf(NotFoundException.class);
        verify(batchRepository, never()).save(any());
    }

    @Test
    void processLineWithoutLocationLeavesItForAutoPlacement() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity line = givenLine(false);
        givenProduct(false);

        service.processLine(form(10, "", 0), 0);

        assertThat(line.getLocationID()).isZero();
        verify(inventoryService).receive(77, PRODUCT_ID, 1, 10, STOREKEEPER_ID);
    }

    @Test
    void processLineRejectsWrongDateFormat() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        givenLine(false);
        givenProduct(false);

        assertThatThrownBy(() -> service.processLine(form(10, "31.05.2027"), 0))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("формат даты");
    }

    // ---------- статусы накладной ----------

    @Test
    void startAndCompleteFollowStatusOrder() {
        ReceivingEntity receiving = givenReceiving(ReceivingStatus.WAITING);
        receiving.setEmployeeID(0);

        assertThatThrownBy(() -> service.complete(RECEIVING_ID)).isInstanceOf(WmsException.class);
        assertThatThrownBy(() -> service.start(RECEIVING_ID, 0, null)).hasMessageContaining("кладовщик");

        service.start(RECEIVING_ID, STOREKEEPER_ID, null);
        assertThat(receiving.getStatusID()).isEqualTo(ReceivingStatus.IN_PROGRESS.getCode());
        assertThat(receiving.getEmployeeID()).isEqualTo(STOREKEEPER_ID);
        assertThat(receiving.getGetReceivingDate()).isNotNull();

        assertThatThrownBy(() -> service.start(RECEIVING_ID, STOREKEEPER_ID, null)).isInstanceOf(WmsException.class);

        service.complete(RECEIVING_ID);
        assertThat(receiving.getStatusID()).isEqualTo(ReceivingStatus.COMPLETED.getCode());
    }

    @Test
    void completeRequiresAllLinesProcessed() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity done = new ReceivingLineEntity(11, 10, 10, null, RECEIVING_ID, 101, PRODUCT_ID, true, false, false, "");
        ReceivingLineEntity open = new ReceivingLineEntity(12, 10, 0, null, RECEIVING_ID, 0, PRODUCT_ID, false, false, false, "");
        when(lineRepository.findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(RECEIVING_ID, false)).thenReturn(new ArrayList<>(List.of(done, open)));

        assertThatThrownBy(() -> service.complete(RECEIVING_ID))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("не обработано строк — 1");
    }

    // ---------- отмена строки и накладной ----------

    @Test
    void revertLineWritesOffStockAndReopensLine() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity line = givenLine(true);
        line.setQuantityReceived(100);
        BatchEntity batch = new BatchEntity(77, PRODUCT_ID, SUPPLIER_ID, null, 100, null, LINE_ID, false);
        when(batchRepository.findActiveByReceivingLine(LINE_ID)).thenReturn(List.of(batch));
        when(inventoryService.getQuantity(77, 1)).thenReturn(100);

        service.revertLine(LINE_ID, 4);

        verify(inventoryService).writeOff(77, 1, 100, 4, MovementType.ADJUSTMENT);
        assertThat(batch.getDel()).isTrue();
        assertThat(line.getComplete()).isFalse();
        assertThat(line.getQuantityReceived()).isZero();
    }

    @Test
    void revertLineIsForbiddenWhenRoutedOrMoved() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity line = givenLine(true);
        line.setPlacementRoute(true);
        assertThatThrownBy(() -> service.revertLine(LINE_ID, 0)).hasMessageContaining("маршрут");

        line.setPlacementRoute(false);
        BatchEntity batch = new BatchEntity(77, PRODUCT_ID, SUPPLIER_ID, null, 100, null, LINE_ID, false);
        when(batchRepository.findActiveByReceivingLine(LINE_ID)).thenReturn(List.of(batch));
        when(inventoryService.getQuantity(77, 1)).thenReturn(60);
        assertThatThrownBy(() -> service.revertLine(LINE_ID, 0)).hasMessageContaining("перемещена");
        verify(inventoryService, never()).writeOff(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void revertLineRequiresAcceptedLine() {
        givenReceiving(ReceivingStatus.IN_PROGRESS);
        givenLine(false);
        assertThatThrownBy(() -> service.revertLine(LINE_ID, 0)).hasMessageContaining("ещё не принята");
    }

    @Test
    void cancelRevertsAcceptedLinesAndSetsStatus() {
        ReceivingEntity receiving = givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity accepted = new ReceivingLineEntity(11, 10, 10, null, RECEIVING_ID, 101, PRODUCT_ID, true, false, false, "");
        ReceivingLineEntity open = new ReceivingLineEntity(12, 10, 0, null, RECEIVING_ID, 0, PRODUCT_ID, false, false, false, "");
        when(lineRepository.findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(RECEIVING_ID, false)).thenReturn(new ArrayList<>(List.of(accepted, open)));
        BatchEntity batch = new BatchEntity(77, PRODUCT_ID, SUPPLIER_ID, null, 10, null, 11, false);
        when(batchRepository.findActiveByReceivingLine(11)).thenReturn(List.of(batch));
        when(inventoryService.getQuantity(77, 1)).thenReturn(10);

        service.cancel(RECEIVING_ID, 0);

        verify(inventoryService).writeOff(77, 1, 10, STOREKEEPER_ID, MovementType.ADJUSTMENT);
        assertThat(accepted.getComplete()).isFalse();
        assertThat(receiving.getStatusID()).isEqualTo(ReceivingStatus.CANCELLED.getCode());
    }

    @Test
    void cancelIsForbiddenWhenRoutedOrCompleted() {
        ReceivingEntity receiving = givenReceiving(ReceivingStatus.IN_PROGRESS);
        ReceivingLineEntity routed = new ReceivingLineEntity(11, 10, 10, null, RECEIVING_ID, 101, PRODUCT_ID, true, true, false, "");
        when(lineRepository.findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(RECEIVING_ID, false)).thenReturn(new ArrayList<>(List.of(routed)));
        assertThatThrownBy(() -> service.cancel(RECEIVING_ID, 0)).hasMessageContaining("маршрут");

        receiving.setStatusID(ReceivingStatus.COMPLETED.getCode());
        assertThatThrownBy(() -> service.cancel(RECEIVING_ID, 0)).hasMessageContaining("Завершена");
        assertThat(receiving.getStatusID()).isEqualTo(ReceivingStatus.COMPLETED.getCode());
    }

    // ---------- загрузка из ERP ----------

    @Test
    void createFromErpRejectsDuplicateDocument() {
        when(receivingRepository.findFirstByDocumentNumberAndStatusIDNot("З30016546", ReceivingStatus.CANCELLED.getCode()))
                .thenReturn(Optional.of(new ReceivingEntity(5, null, null, "З30016546", 0, 0, 1, false)));

        assertThatThrownBy(() -> service.createFromErp(json(erpLine("029952", "1"))))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("уже загружена (id 5)");
        verify(receivingRepository, never()).save(any());
    }


    @Test
    void createFromErpUsesSavedIdAndSkipsUnknownProducts() {
        when(supplierRepository.findTopBySupplierCode("П000019")).thenReturn(Optional.of(supplier()));
        when(productRepository.findOneByProductCode("029952")).thenReturn(Optional.of(product(PRODUCT_ID, false)));
        when(productRepository.findOneByProductCode("999999")).thenReturn(Optional.empty());

        ReceivingProcessService.ErpImportResult result = service.createFromErp(json(
                erpLine("029952", "200"), erpLine("999999", "5")));

        assertThat(result.receivingId()).isEqualTo(RECEIVING_ID);
        assertThat(result.linesCreated()).isEqualTo(1);
        assertThat(result.missingProductCodes()).containsExactly("999999");
        ArgumentCaptor<ReceivingLineEntity> line = ArgumentCaptor.forClass(ReceivingLineEntity.class);
        verify(lineRepository, times(1)).save(line.capture());
        assertThat(line.getValue().getReceivingID()).isEqualTo(RECEIVING_ID);
        assertThat(line.getValue().getQuantityProduct()).isEqualTo(200);
        verify(receivingRepository, never()).findFirstByOrderByReceivingIDDesc();
    }

    @Test
    void createFromErpFailsForUnknownSupplier() {
        when(supplierRepository.findTopBySupplierCode(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createFromErp(json(erpLine("029952", "1"))))
                .isInstanceOf(NotFoundException.class);
        verify(receivingRepository, never()).save(any());
    }

    @Test
    void createFromErpRejectsBadQuantity() {
        when(supplierRepository.findTopBySupplierCode(any())).thenReturn(Optional.of(supplier()));
        assertThatThrownBy(() -> service.createFromErp(json(erpLine("029952", "abc"))))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("количество");
        verify(receivingRepository, never()).save(any());
    }

    // ---------- вспомогательные ----------

    private ReceivingEntity givenReceiving(ReceivingStatus status) {
        ReceivingEntity r = new ReceivingEntity(RECEIVING_ID, null, null, "Z-1", status.getCode(),
                STOREKEEPER_ID, SUPPLIER_ID, false);
        when(receivingRepository.findById(RECEIVING_ID)).thenReturn(Optional.of(r));
        return r;
    }

    private ReceivingLineEntity givenLine(boolean complete) {
        ReceivingLineEntity l = new ReceivingLineEntity(LINE_ID, 100, 0, null, RECEIVING_ID, 0, PRODUCT_ID,
                complete, false, false, "");
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(l));
        return l;
    }

    private void givenProduct(boolean expirationRequired) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product(PRODUCT_ID, expirationRequired)));
    }

    private static ProductEntity product(int id, boolean expirationRequired) {
        ProductEntity p = new ProductEntity();
        p.setProductID(id);
        p.setProductName("Товар " + id);
        p.setExpirationDateRequired(expirationRequired);
        return p;
    }

    private static SupplierEntity supplier() {
        SupplierEntity s = new SupplierEntity();
        s.setSupplierID(SUPPLIER_ID);
        s.setSupplierCode("П000019");
        return s;
    }

    private static ReceivingLineForm form(int qntFact, String expirationDate) {
        return form(qntFact, expirationDate, 101);
    }

    private static ReceivingLineForm form(int qntFact, String expirationDate, int locationId) {
        return ReceivingLineForm.builder()
                .numLine(LINE_ID)
                .orderId(RECEIVING_ID)
                .productId(PRODUCT_ID)
                .qntFact(qntFact)
                .expirationDate(expirationDate)
                .locationId(locationId)
                .note("")
                .build();
    }

    private static ReceivingLineERP erpLine(String code, String qnt) {
        ReceivingLineERP l = new ReceivingLineERP();
        l.code = code;
        l.qnt = qnt;
        return l;
    }

    private static ReceivingJSON json(ReceivingLineERP... lines) {
        ReceivingERP header = new ReceivingERP();
        header.orderERP = "З30016546";
        header.supplierCode = "П000019";
        header.orderDate = "2026-10-09 08:30:00.0";
        return new ReceivingJSON(header, new ArrayList<>(List.of(lines)));
    }
}
