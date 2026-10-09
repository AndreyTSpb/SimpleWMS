package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.ReceivingJSON;
import com.tynyany.simplewmsv2.entity.ReceivingLineForm;

import java.sql.Timestamp;
import java.util.List;

/**
 * Бизнес-процесс приёмки товара: от получения накладной из ERP до поступления остатка в зону приёмки.
 */
public interface ReceivingProcessService {

    /**
     * Результат загрузки накладной из ERP.
     *
     * @param receivingId          id созданной накладной
     * @param linesCreated         сколько строк создано
     * @param missingProductCodes  коды товаров, которых нет в справочнике (строки пропущены)
     */
    record ErpImportResult(int receivingId, int linesCreated, List<String> missingProductCodes) {}

    /** Создание приходной накладной по данным ERP (статус «В ожидании»). */
    ErpImportResult createFromErp(ReceivingJSON json);

    /** Начало приёмки: накладная переходит в статус «В процессе», назначается кладовщик. */
    void start(int receivingId, int employeeId, Timestamp startDate);

    /** Завершение приёмки: статус «Завершена». */
    void complete(int receivingId);

    /**
     * Приёмка строки накладной: фиксируется фактическое количество, создаётся партия,
     * товар поступает на остаток в ячейку зоны приёмки. Выполняется атомарно.
     *
     * @param executorEmployeeId сотрудник, выполнивший приёмку (записывается в журнал движений);
     *                           0 — кладовщик, назначенный на накладную
     */
    void processLine(ReceivingLineForm form, int executorEmployeeId);
}
