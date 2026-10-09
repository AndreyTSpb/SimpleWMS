package com.tynyany.simplewmsv2.controller;

import jakarta.servlet.http.HttpServletResponse;
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

    // Любой метод: сюда перенаправляется отказ в доступе, в т.ч. для POST-запросов
    @RequestMapping("/403")
    public String error403(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("errorMessage", "Недостаточно прав для этого раздела. Обратитесь к администратору.");
        model.addAttribute("status", 403);
        return "error";
    }

    @GetMapping("/404")
    public String error404() {
        return "404.html";
    }
}
