package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ReportsController {

    //warehouse_status
    @GetMapping("/warehouse_status")
    public String WarehouseStatus(Model model) {
        model.addAttribute("title", "Отчет о загруженности мест хранения на складе");
        return "home";
    }
    @GetMapping("/shipment_report")
    public String ShipmentReport(Model model) {
        model.addAttribute("title", "Сводный отчет по отгрузке");
        return "home";
    }
    @GetMapping("/income_report")
    public String IncomeReport(Model model) {
        model.addAttribute("title", "Сводный отчет по принятому товару от поставщиков");
        return "home";
    }

    @GetMapping("/report_pickers")
    public String ReportPickers(Model model){
        model.addAttribute("title", "Сводный отчет по выполненной работе комплектовщиков");
        return "home";
    }

    @GetMapping("/product_report")
    public String ProductReport(Model model){
        model.addAttribute("title", "Сводный отчет по товарам");
        return "home";
    }
}
