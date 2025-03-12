package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wiki")
@RequiredArgsConstructor
public class WikiController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Инструкции по работе с SimpleWMS");
        return "users";
    }
}
