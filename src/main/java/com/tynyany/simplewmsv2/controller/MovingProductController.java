package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/moving_product")
@RequiredArgsConstructor
public class MovingProductController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Система управления складом");
        return "home";
    }
}
