package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Stock;
import com.tynyany.simplewmsv2.models.GenerationProductPlacementRoutes;
import com.tynyany.simplewmsv2.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product_placement")
@RequiredArgsConstructor
public class ProductPlacementController {

    final StockService stockService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Размещенеи товара на складе");
        GenerationProductPlacementRoutes generationProductPlacementRoutes = new GenerationProductPlacementRoutes(stockService);
        return "product_placement_list";
    }
}
