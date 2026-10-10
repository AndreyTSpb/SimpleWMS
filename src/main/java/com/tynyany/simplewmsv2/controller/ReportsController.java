package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.domain.MovementType;
import com.tynyany.simplewmsv2.service.ReportService;
import com.tynyany.simplewmsv2.service.StorageLocations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Отчёты: состояние склада, журнал движений, сводки по отгрузкам, поставкам и работе сотрудников.
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportService reportService;
    private final StorageLocations storageLocations;

    @GetMapping("/warehouse_status")
    public String warehouseStatus(@RequestParam(value = "view", defaultValue = "product") String view,
                                  @RequestParam(value = "product", defaultValue = "") String product,
                                  @RequestParam(value = "zone", required = false) Integer zone,
                                  @RequestParam(value = "location", defaultValue = "") String location,
                                  @RequestParam(value = "expiring", defaultValue = "false") boolean expiring,
                                  Model model) {
        ReportService.StockFilter filter = new ReportService.StockFilter(product, zone, location, expiring);
        model.addAttribute("title", "Состояние склада");
        model.addAttribute("view", view);
        model.addAttribute("product", product);
        model.addAttribute("zone", zone);
        model.addAttribute("location", location);
        model.addAttribute("expiring", expiring);
        model.addAttribute("zones", storageLocations.zones());
        switch (view) {
            case "batch" -> model.addAttribute("batches", reportService.stockByBatch(filter));
            case "cells" -> model.addAttribute("cells", reportService.cellLoad(filter));
            default -> {
                model.addAttribute("view", "product");
                model.addAttribute("products", reportService.stockByProduct(filter));
            }
        }
        return "warehouse_status";
    }

    @GetMapping("/movement_journal")
    public String movementJournal(@RequestParam(value = "from", defaultValue = "") String from,
                                  @RequestParam(value = "to", defaultValue = "") String to,
                                  @RequestParam(value = "type", required = false) Integer type,
                                  @RequestParam(value = "product", defaultValue = "") String product,
                                  @RequestParam(value = "batch", required = false) Integer batch,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  Model model) {
        LocalDate fromDate = parseDate(from, null);
        LocalDate toDate = parseDate(to, null);
        model.addAttribute("title", "Журнал движений товара");
        model.addAttribute("from", fromDate != null ? fromDate.toString() : "");
        model.addAttribute("to", toDate != null ? toDate.toString() : "");
        model.addAttribute("type", type);
        model.addAttribute("product", product);
        model.addAttribute("batch", batch);
        model.addAttribute("types", MovementType.values());
        model.addAttribute("result", reportService.movements(
                new ReportService.MovementFilter(fromDate, toDate, type, product, batch), page));
        return "movement_journal";
    }

    @GetMapping("/shipment_report")
    public String shipmentReport(@RequestParam(value = "from", defaultValue = "") String from,
                                 @RequestParam(value = "to", defaultValue = "") String to, Model model) {
        LocalDate[] period = period(from, to, model);
        model.addAttribute("title", "Отчёт по отгрузкам");
        model.addAttribute("report", reportService.shipmentReport(period[0], period[1]));
        return "shipment_report";
    }

    @GetMapping("/income_report")
    public String incomeReport(@RequestParam(value = "from", defaultValue = "") String from,
                               @RequestParam(value = "to", defaultValue = "") String to, Model model) {
        LocalDate[] period = period(from, to, model);
        model.addAttribute("title", "Отчёт по поставкам");
        model.addAttribute("report", reportService.incomeReport(period[0], period[1]));
        return "income_report";
    }

    @GetMapping("/report_pickers")
    public String pickersReport(@RequestParam(value = "from", defaultValue = "") String from,
                                @RequestParam(value = "to", defaultValue = "") String to, Model model) {
        LocalDate[] period = period(from, to, model);
        model.addAttribute("title", "Отчёт по работе комплектовщиков");
        model.addAttribute("rows", reportService.employeeWork(period[0], period[1]));
        return "report_pickers";
    }

    /** Период отчёта: по умолчанию — с начала текущего месяца по сегодня. */
    private static LocalDate[] period(String from, String to, Model model) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = parseDate(to, today);
        LocalDate fromDate = parseDate(from, toDate.withDayOfMonth(1));
        if (fromDate.isAfter(toDate)) {
            LocalDate swap = fromDate;
            fromDate = toDate;
            toDate = swap;
        }
        model.addAttribute("from", fromDate.toString());
        model.addAttribute("to", toDate.toString());
        return new LocalDate[]{fromDate, toDate};
    }

    private static LocalDate parseDate(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
}
