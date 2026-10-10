package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ABCEntity;
import com.tynyany.simplewmsv2.dao.CategoryEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.dto.erp.ErpProduct;
import com.tynyany.simplewmsv2.dto.erp.ErpSupplier;
import com.tynyany.simplewmsv2.dto.erp.ErpViews;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.repository.ABCRepository;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.CategoryRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderLineRepository;
import com.tynyany.simplewmsv2.repository.CustomerOrderRepository;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.repository.ShipmentRepository;
import com.tynyany.simplewmsv2.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultErpServiceTest {

    private ProductRepository productRepository;
    private SupplierRepository supplierRepository;
    private CategoryRepository categoryRepository;
    private ABCRepository abcRepository;
    private CustomerOrderRepository orderRepository;
    private OrderService orderService;
    private DefaultErpService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        supplierRepository = mock(SupplierRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        abcRepository = mock(ABCRepository.class);
        orderRepository = mock(CustomerOrderRepository.class);
        orderService = mock(OrderService.class);
        service = new DefaultErpService(productRepository, supplierRepository, mock(CustomerRepository.class),
                categoryRepository, abcRepository, mock(ReceivingRepository.class), mock(ReceivingLineRepository.class),
                mock(BatchRepository.class), orderRepository, mock(CustomerOrderLineRepository.class),
                mock(ShipmentRepository.class), orderService);

        SupplierEntity supplier = new SupplierEntity(7, "Поставщик", "П1", "", "", "", false);
        when(supplierRepository.findTopBySupplierCode("П1")).thenReturn(Optional.of(supplier));
        when(abcRepository.findFirstByCodeIgnoreCase("A")).thenReturn(Optional.of(new ABCEntity(1, "A", "", false)));
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            CategoryEntity c = inv.getArgument(0);
            c.setCategoryID(33);
            return c;
        });
    }

    private static ErpProduct product(String code, String supplier) {
        return new ErpProduct(code, "Товар " + code, null, supplier, "Новая категория", "a", 0.5F, 1.2F, true, "460", "200");
    }

    @Test
    void upsertProductsCreatesNewAndUpdatesExisting() {
        ProductEntity existing = new ProductEntity();
        existing.setProductID(5);
        existing.setProductCode("002");
        existing.setDescription("старое описание");
        existing.setDel(false);
        when(productRepository.findOneByProductCode("001")).thenReturn(Optional.empty());
        when(productRepository.findOneByProductCode("002")).thenReturn(Optional.of(existing));

        ErpViews.UpsertResult result = service.upsertProducts(List.of(product("001", "П1"), product("002", "П1")));

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        ArgumentCaptor<ProductEntity> saved = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository, times(2)).save(saved.capture());
        ProductEntity created = saved.getAllValues().get(0);
        assertThat(created.getSupplierID()).isEqualTo(7);
        assertThat(created.getAbcID()).isEqualTo(1);
        assertThat(created.getCategoryID()).isEqualTo(33);
        assertThat(created.getVolume()).isEqualTo(1.2F);
        assertThat(created.getExpirationDateRequired()).isTrue();
        assertThat(created.getDel()).isFalse();
        // необязательное поле, не переданное ERP, не затирается
        assertThat(saved.getAllValues().get(1).getDescription()).isEqualTo("старое описание");
    }

    @Test
    void unknownSupplierRejectsWholeBatch() {
        assertThatThrownBy(() -> service.upsertProducts(List.of(product("001", "НЕТ"))))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("НЕТ");
        verify(productRepository, never()).save(any());
    }

    @Test
    void duplicateCodesInRequestAreRejected() {
        assertThatThrownBy(() -> service.upsertSuppliers(List.of(
                new ErpSupplier("S1", "A", null, null, null), new ErpSupplier(" S1", "B", null, null, null))))
                .isInstanceOf(WmsException.class)
                .hasMessageContaining("повторяется");
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void cancelOrderByNumberDelegatesToOrderService() {
        CustomerOrderEntity order = new CustomerOrderEntity();
        order.setOrderId(12);
        order.setDel(false);
        when(orderRepository.findFirstByDocumentNumberOrderByOrderIdDesc("ПР-1")).thenReturn(Optional.of(order));

        service.cancelOrder(" ПР-1 ");

        verify(orderService).cancel(12);
    }

    @Test
    void unknownOrderNumberIsNotFound() {
        when(orderRepository.findFirstByDocumentNumberOrderByOrderIdDesc(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getOrder("НЕТ")).isInstanceOf(NotFoundException.class);
    }
}
