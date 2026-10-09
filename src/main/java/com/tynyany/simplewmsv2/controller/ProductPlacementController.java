package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.models.UpdateMessageAlert;
import com.tynyany.simplewmsv2.service.PlacementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product_placement")
@RequiredArgsConstructor
public class ProductPlacementController {

    private final PlacementService placementService;

    @GetMapping
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("title", "Маршруты размещения товара");
        model.addAttribute("routes", placementService.getRoutes());

        UpdateMessageAlert alert = new UpdateMessageAlert(request, response);
        if (alert.updateMessage != null) {
            model.addAttribute("updateMessage", alert.updateMessage);
        }
        return "product_placement_list";
    }
}
