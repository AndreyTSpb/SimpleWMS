package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dto.erp.ErpCustomer;
import com.tynyany.simplewmsv2.dto.erp.ErpProduct;
import com.tynyany.simplewmsv2.dto.erp.ErpSupplier;
import com.tynyany.simplewmsv2.dto.erp.ErpViews;

import java.time.LocalDate;
import java.util.List;

/**
 * Обмен с ERP: загрузка справочников и ответы о состоянии документов склада.
 * Загрузка документов — {@link ReceivingProcessService#createFromErp}, {@link OrderService#createFromErp}.
 */
public interface ErpService {

    /** Товары: создание или обновление по коду. Все записи сохраняются вместе или ни одной. */
    ErpViews.UpsertResult upsertProducts(List<ErpProduct> products);

    ErpViews.UpsertResult upsertSuppliers(List<ErpSupplier> suppliers);

    ErpViews.UpsertResult upsertCustomers(List<ErpCustomer> customers);

    /** Приходная накладная по номеру ERP (последняя загруженная с этим номером). */
    ErpViews.Receiving getReceiving(String number);

    /** Завершённые приёмки, начатые не раньше from. */
    List<ErpViews.Receiving> getCompletedReceivings(LocalDate from);

    /** Заказ по номеру ERP (последний загруженный с этим номером). */
    ErpViews.Order getOrder(String number);

    /** Отмена заказа по номеру ERP (до передачи в сборку). */
    void cancelOrder(String number);
}
