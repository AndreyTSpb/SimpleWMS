package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//dispatch_product
@Controller
@RequestMapping("/shipping_list")
@RequiredArgsConstructor
public class ShippingController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Список отгрузок");
        return "users";
    }

    @GetMapping("/add")
    public String addProduct(Model model) {
        model.addAttribute("title", "Создать отгрузку");
        return "add_user";
    }
}
