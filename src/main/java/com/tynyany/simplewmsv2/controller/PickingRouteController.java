package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.UpdateMessageAlert;
import com.tynyany.simplewmsv2.security.CurrentUser;
import com.tynyany.simplewmsv2.service.PickingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Комплектация заказов: маршруты сборки, подтверждение строк, печать сборочного листа.
 */
@Slf4j
@Controller
@RequestMapping("/picking_route_list")
@RequiredArgsConstructor
public class PickingRouteController {

    private static final String REDIRECT = "redirect:/picking_route_list";

    private final PickingService pickingService;
    private final WmsProperties wmsProperties;

    @GetMapping
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("title", "Комплектация заказов");
        model.addAttribute("routes", pickingService.getRoutes());
        UpdateMessageAlert alert = new UpdateMessageAlert(request, response);
        if (alert.updateMessage != null) {
            model.addAttribute("updateMessage", alert.updateMessage);
        }
        return "picking_route_list";
    }

    @PostMapping("/start/{id}")
    public String start(@PathVariable("id") int routeId, HttpServletResponse response) {
        run(response, () -> pickingService.startRoute(routeId, CurrentUser.employeeId()),
                "Маршрут сборки " + routeId + " взят в работу");
        return REDIRECT;
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam("lineId") int lineId, @RequestParam("quantity") int quantity,
                          @RequestParam(value = "label", defaultValue = "") String label,
                          HttpServletResponse response) {
        try {
            boolean routeDone = pickingService.confirmLine(lineId, quantity, CurrentUser.employeeId());
            String message = (label.isBlank() ? "Строка" : label) + ": собрано " + quantity + " шт.";
            alert(response, routeDone ? message + " Маршрут завершён - заказ собран" : message);
        } catch (WmsException e) {
            log.warn("Операция сборки отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable("id") int routeId, HttpServletResponse response) {
        run(response, () -> pickingService.cancelRoute(routeId),
                "Маршрут сборки " + routeId + " отменён - заказ снова в резерве");
        return REDIRECT;
    }

    /** Сборочный лист для печати. */
    @GetMapping("/print/{id}")
    public String print(@PathVariable("id") int routeId, Model model) {
        model.addAttribute("route", pickingService.getRoute(routeId));
        model.addAttribute("companyName", wmsProperties.getCompany().getName());
        return "piccking_list";
    }

    private void run(HttpServletResponse response, Runnable action, String successMessage) {
        try {
            action.run();
            alert(response, successMessage);
        } catch (WmsException e) {
            log.warn("Операция сборки отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
    }

    private void alert(HttpServletResponse response, String message) {
        response.addCookie(new AddCookie("alertMessage", message).getCookie());
    }
}
