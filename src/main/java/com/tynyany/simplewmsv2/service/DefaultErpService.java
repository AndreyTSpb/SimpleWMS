package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ABCEntity;
import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.dao.CategoryEntity;
import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderEntity;
import com.tynyany.simplewmsv2.dao.CustomerOrderLineEntity;
import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.dao.ShipmentEntity;
import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.domain.ReceivingStatus;
import com.tynyany.simplewmsv2.dto.erp.ErpCustomer;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultErpService implements ErpService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final ABCRepository abcRepository;
    private final ReceivingRepository receivingRepository;
    private final ReceivingLineRepository receivingLineRepository;
    private final BatchRepository batchRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderLineRepository orderLineRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;

    // ---------- справочники ----------

    @Override
    @Transactional
    public ErpViews.UpsertResult upsertProducts(List<ErpProduct> products) {
        requireUniqueCodes(products.stream().map(ErpProduct::code).toList(), "товара");
        int created = 0;
        int updated = 0;
        for (ErpProduct item : products) {
            SupplierEntity supplier = supplierRepository.findTopBySupplierCode(item.supplierCode().trim())
                    .orElseThrow(() -> new NotFoundException("Товар " + item.code() + ": поставщик с кодом "
                            + item.supplierCode() + " не найден"));
            Optional<ProductEntity> existing = productRepository.findOneByProductCode(item.code().trim());
            ProductEntity product = existing.orElseGet(ProductEntity::new);
            product.setProductCode(item.code().trim());
            product.setProductName(item.name().trim());
            if (item.description() != null) product.setDescription(item.description());
            product.setSupplierID(supplier.getSupplierID());
            if (!isBlank(item.category())) product.setCategoryID(category(item.category().trim()).getCategoryID());
            if (!isBlank(item.abc())) {
                ABCEntity abc = abcRepository.findFirstByCodeIgnoreCase(item.abc().trim().toUpperCase())
                        .orElseThrow(() -> new NotFoundException("ABC-класс " + item.abc() + " не найден"));
                product.setAbcID(abc.getAbcID());
            }
            if (item.weight() != null) product.setWeight(item.weight());
            if (item.volume() != null) product.setVolume(item.volume());
            if (item.expirationRequired() != null) product.setExpirationDateRequired(item.expirationRequired());
            if (item.extBarcode() != null) product.setExtBarCode(item.extBarcode());
            if (item.intBarcode() != null) product.setIntBarCode(item.intBarcode());
            if (product.getDel() == null) product.setDel(false);
            if (product.getExpirationDateRequired() == null) product.setExpirationDateRequired(false);
            productRepository.save(product);
            if (existing.isPresent()) updated++; else created++;
        }
        log.info("ERP: товары — создано {}, обновлено {}", created, updated);
        return new ErpViews.UpsertResult(created, updated);
    }

    @Override
    @Transactional
    public ErpViews.UpsertResult upsertSuppliers(List<ErpSupplier> suppliers) {
        requireUniqueCodes(suppliers.stream().map(ErpSupplier::code).toList(), "поставщика");
        int created = 0;
        int updated = 0;
        for (ErpSupplier item : suppliers) {
            Optional<SupplierEntity> existing = supplierRepository.findTopBySupplierCode(item.code().trim());
            SupplierEntity supplier = existing.orElseGet(SupplierEntity::new);
            supplier.setSupplierCode(item.code().trim());
            supplier.setSupplierName(item.name().trim());
            if (item.contactPerson() != null) supplier.setContactPerson(item.contactPerson());
            if (item.phone() != null) supplier.setPhoneNumber(item.phone());
            if (item.email() != null) supplier.setEmail(item.email());
            if (supplier.getDel() == null) supplier.setDel(false);
            supplierRepository.save(supplier);
            if (existing.isPresent()) updated++; else created++;
        }
        log.info("ERP: поставщики — создано {}, обновлено {}", created, updated);
        return new ErpViews.UpsertResult(created, updated);
    }

    @Override
    @Transactional
    public ErpViews.UpsertResult upsertCustomers(List<ErpCustomer> customers) {
        requireUniqueCodes(customers.stream().map(ErpCustomer::code).toList(), "клиента");
        int created = 0;
        int updated = 0;
        for (ErpCustomer item : customers) {
            Optional<CustomerEntity> existing = customerRepository.findByCustomerCode(item.code().trim());
            CustomerEntity customer = existing.orElseGet(CustomerEntity::new);
            customer.setCustomerCode(item.code().trim());
            customer.setCustomerName(item.name().trim());
            if (item.phone() != null) customer.setPhone(item.phone());
            if (item.email() != null) customer.setEmail(item.email());
            if (item.address() != null) customer.setAddress(item.address());
            if (item.workingFrom() != null) customer.setWorkingTimeStr(item.workingFrom());
            if (item.workingTo() != null) customer.setWorkingTimeEnd(item.workingTo());
            if (customer.getDel() == null) customer.setDel(false);
            customerRepository.save(customer);
            if (existing.isPresent()) updated++; else created++;
        }
        log.info("ERP: клиенты — создано {}, обновлено {}", created, updated);
        return new ErpViews.UpsertResult(created, updated);
    }

    // ---------- документы ----------

    @Override
    @Transactional(readOnly = true)
    public ErpViews.Receiving getReceiving(String number) {
        ReceivingEntity receiving = receivingRepository.findFirstByDocumentNumberOrderByReceivingIDDesc(number.trim())
                .orElseThrow(() -> new NotFoundException("Приходная накладная " + number + " не найдена"));
        return toView(receiving);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErpViews.Receiving> getCompletedReceivings(LocalDate from) {
        List<ErpViews.Receiving> result = new ArrayList<>();
        for (ReceivingEntity receiving : receivingRepository.findByStatusSince(
                ReceivingStatus.COMPLETED.getCode(), Timestamp.valueOf(from.atStartOfDay()))) {
            result.add(toView(receiving));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ErpViews.Order getOrder(String number) {
        CustomerOrderEntity order = findOrder(number);
        SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        List<ErpViews.OrderLine> lines = new ArrayList<>();
        for (CustomerOrderLineEntity line : orderLineRepository.findByOrderIdOrderByLineIdAsc(order.getOrderId())) {
            Optional<ProductEntity> product = productRepository.findById(line.getProductId());
            lines.add(new ErpViews.OrderLine(
                    product.map(ProductEntity::getProductCode).orElse(String.valueOf(line.getProductId())),
                    product.map(ProductEntity::getProductName).orElse(""),
                    line.getQtyOrdered(), line.getQtyReserved(), line.getQtyPicked(), line.getQtyShipped()));
        }
        return new ErpViews.Order(order.getOrderId(), order.getDocumentNumber(),
                customerRepository.findById(order.getCustomerId()).map(CustomerEntity::getCustomerCode).orElse(""),
                order.getOrderDate() != null ? dateTime.format(order.getOrderDate()) : null,
                order.getShipDate() != null ? date.format(order.getShipDate()) : null,
                OrderStatus.fromCode(order.getStatusId()).getTitle(),
                shipmentRepository.findActiveByOrderId(order.getOrderId()).map(ShipmentEntity::getDocumentNumber).orElse(null),
                lines);
    }

    @Override
    @Transactional
    public void cancelOrder(String number) {
        orderService.cancel(findOrder(number).getOrderId());
    }

    // ---------- вспомогательные ----------

    private ErpViews.Receiving toView(ReceivingEntity receiving) {
        SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        List<ErpViews.ReceivingLine> lines = new ArrayList<>();
        for (ReceivingLineEntity line : receivingLineRepository
                .findAllByReceivingIDAndDelOrderByReceivingLineIDAsc(receiving.getReceivingID(), false)) {
            Optional<ProductEntity> product = productRepository.findById(line.getProductID());
            String expiry;
            if (line.getExpirationDate() != null) {
                expiry = date.format(line.getExpirationDate());
            } else {
                expiry = batchRepository.findActiveByReceivingLine(line.getReceivingLineID()).stream()
                        .map(BatchEntity::getExpiry_date).filter(java.util.Objects::nonNull)
                        .findFirst().map(date::format).orElse(null);
            }
            lines.add(new ErpViews.ReceivingLine(
                    product.map(ProductEntity::getProductCode).orElse(String.valueOf(line.getProductID())),
                    product.map(ProductEntity::getProductName).orElse(""),
                    line.getQuantityProduct(), line.getQuantityReceived(),
                    Boolean.TRUE.equals(line.getComplete()), expiry));
        }
        return new ErpViews.Receiving(receiving.getReceivingID(), receiving.getDocumentNumber(),
                supplierRepository.findById(receiving.getSupplierID()).map(SupplierEntity::getSupplierCode).orElse(""),
                receiving.getReceivingDate() != null ? dateTime.format(receiving.getReceivingDate()) : null,
                ReceivingStatus.fromCode(receiving.getStatusID()).getTitle(),
                receiving.getGetReceivingDate() != null ? dateTime.format(receiving.getGetReceivingDate()) : null,
                lines);
    }

    private CustomerOrderEntity findOrder(String number) {
        return orderRepository.findFirstByDocumentNumberOrderByOrderIdDesc(number.trim())
                .filter(o -> !Boolean.TRUE.equals(o.getDel()))
                .orElseThrow(() -> new NotFoundException("Заказ " + number + " не найден"));
    }

    private CategoryEntity category(String name) {
        return categoryRepository.findFirstByCategoryNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new CategoryEntity(0, name, "", false)));
    }

    private static void requireUniqueCodes(List<String> codes, String what) {
        Set<String> seen = new HashSet<>();
        for (String code : codes) {
            if (code != null && !seen.add(code.trim())) {
                throw new WmsException("Код " + what + " " + code + " повторяется в запросе");
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
