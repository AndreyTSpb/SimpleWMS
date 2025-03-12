package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order_list")
@RequiredArgsConstructor
public class OrdersController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Список заказов");
        return "users";
    }

    @GetMapping("/view")
    public String view(Model model) {
        model.addAttribute("title", "Создать отгрузку");
        return "add_user";
    }
}
