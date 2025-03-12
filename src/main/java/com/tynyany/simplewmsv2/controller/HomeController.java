package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Система управления складом");
        return "home";
    }

    @GetMapping("/403")
    public String error403() {
        return "error.html";
    }

    @GetMapping("/404")
    public String error404() {
        return "404.html";
    }
}
