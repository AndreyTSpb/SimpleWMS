package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.UpdateMessageAlert;
import com.tynyany.simplewmsv2.security.CurrentUser;
import com.tynyany.simplewmsv2.service.StorageLocations;
import com.tynyany.simplewmsv2.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

/**
 * Перемещение товара между ячейками, содержимое ячейки, поиск «где лежит товар», журнал перемещений.
 */
@Slf4j
@Controller
@RequestMapping("/moving_product")
@RequiredArgsConstructor
public class MovingProductController {

    private final TransferService transferService;
    private final StorageLocations storageLocations;

    @GetMapping
    public String index(@RequestParam(value = "locationId", defaultValue = "0") int locationId,
                        @RequestParam(value = "q", defaultValue = "") String query,
                        Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("title", "Перемещение товара");
        var cells = storageLocations.cells();
        model.addAttribute("cells", cells);
        model.addAttribute("cellIds", cells.stream().map(c -> c.getLocationID()).toList());
        model.addAttribute("locationId", locationId);
        model.addAttribute("q", query);
        model.addAttribute("cellContents", locationId > 0 ? transferService.cellContents(locationId) : java.util.List.of());
        model.addAttribute("found", transferService.findProduct(query));
        model.addAttribute("moves", transferService.recentMoves());

        UpdateMessageAlert alert = new UpdateMessageAlert(request, response);
        if (alert.updateMessage != null) {
            model.addAttribute("updateMessage", alert.updateMessage);
        }
        return "moving_product";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam("batchId") int batchId,
                           @RequestParam("fromLocationId") int fromLocationId,
                           @RequestParam("toLocationId") int toLocationId,
                           @RequestParam("quantity") int quantity,
                           @RequestParam(value = "q", defaultValue = "") String query,
                           HttpServletResponse response) {
        String message;
        try {
            TransferService.TransferResult result =
                    transferService.transfer(batchId, fromLocationId, toLocationId, quantity, CurrentUser.employeeId());
            message = "Перемещено " + quantity + " шт. в ячейку " + result.targetCode();
            if (result.overCapacity()) {
                message += String.format(Locale.ROOT, ". Внимание: ячейка заполнена на %.0f из %.0f л",
                        result.usedVolume(), result.capacity());
            }
        } catch (WmsException e) {
            log.warn("Перемещение отклонено: {}", e.getMessage());
            message = e.getMessage();
        }
        response.addCookie(new AddCookie("alertMessage", message).getCookie());
        return "redirect:" + UriComponentsBuilder.fromPath("/moving_product")
                .queryParam("locationId", fromLocationId)
                .queryParam("q", query)
                .encode().build().toUriString();
    }
}
