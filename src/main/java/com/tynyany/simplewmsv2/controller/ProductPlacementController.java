package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.UpdateMessageAlert;
import com.tynyany.simplewmsv2.security.CurrentUser;
import com.tynyany.simplewmsv2.service.PlacementService;
import com.tynyany.simplewmsv2.service.StorageLocations;
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
 * Маршруты размещения: формирование, исполнение кладовщиком, печать листа размещения.
 */
@Slf4j
@Controller
@RequestMapping("/product_placement")
@RequiredArgsConstructor
public class ProductPlacementController {

    private static final String REDIRECT = "redirect:/product_placement";

    private final PlacementService placementService;
    private final StorageLocations storageLocations;

    @GetMapping
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("title", "Маршруты размещения товара");
        model.addAttribute("routes", placementService.getRoutes());
        model.addAttribute("cells", storageLocations.cells());

        UpdateMessageAlert alert = new UpdateMessageAlert(request, response);
        if (alert.updateMessage != null) {
            model.addAttribute("updateMessage", alert.updateMessage);
        }
        return "product_placement_list";
    }

    @PostMapping("/generate")
    public String generate(HttpServletResponse response) {
        PlacementService.GenerationResult result = placementService.generateRoutes();
        String message = "Создано маршрутов: " + result.routesCreated() + ", строк: " + result.linesCreated();
        if (result.linesSkipped() > 0) {
            message += ". Не хватило места для строк: " + result.linesSkipped();
        }
        alert(response, message);
        return REDIRECT;
    }

    @PostMapping("/start/{id}")
    public String start(@PathVariable("id") int routeId, HttpServletResponse response) {
        run(response, () -> placementService.startRoute(routeId, CurrentUser.employeeId()),
                "Маршрут " + routeId + " взят в работу");
        return REDIRECT;
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam("lineId") int lineId,
                          @RequestParam(value = "locationId", defaultValue = "0") int locationId,
                          HttpServletResponse response) {
        try {
            boolean routeDone = placementService.confirmLine(lineId, locationId, CurrentUser.employeeId());
            alert(response, routeDone ? "Размещение подтверждено, маршрут завершён" : "Размещение подтверждено");
        } catch (WmsException e) {
            log.warn("Операция размещения отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable("id") int routeId, HttpServletResponse response) {
        run(response, () -> placementService.cancelRoute(routeId),
                "Маршрут " + routeId + " отменён, товар снова ожидает размещения");
        return REDIRECT;
    }

    /** Лист размещения для печати. */
    @GetMapping("/print/{id}")
    public String print(@PathVariable("id") int routeId, Model model) {
        model.addAttribute("route", placementService.getRoute(routeId));
        return "placement_print";
    }

    private void run(HttpServletResponse response, Runnable action, String successMessage) {
        try {
            action.run();
            alert(response, successMessage);
        } catch (WmsException e) {
            log.warn("Операция размещения отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
    }

    private void alert(HttpServletResponse response, String message) {
        response.addCookie(new AddCookie("alertMessage", message).getCookie());
    }
}
