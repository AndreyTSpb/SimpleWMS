package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.UpdateMessageAlert;
import com.tynyany.simplewmsv2.security.CurrentUser;
import com.tynyany.simplewmsv2.service.OrderService;
import com.tynyany.simplewmsv2.service.ShippingService;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Отгрузка собранных заказов: заказы к отгрузке, журнал отгрузок, накладная ТОРГ-12.
 */
@Slf4j
@Controller
@RequestMapping("/shipping_list")
@RequiredArgsConstructor
public class ShippingController {

    private static final String REDIRECT = "redirect:/shipping_list";

    private final ShippingService shippingService;
    private final OrderService orderService;
    private final WmsProperties wmsProperties;

    @GetMapping
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("title", "Отгрузка заказов");
        model.addAttribute("ready", orderService.getOrders(OrderStatus.PICKED.getCode()));
        model.addAttribute("shipments", shippingService.getShipments());
        UpdateMessageAlert alert = new UpdateMessageAlert(request, response);
        if (alert.updateMessage != null) {
            model.addAttribute("updateMessage", alert.updateMessage);
        }
        return "shipping_list";
    }

    @PostMapping("/ship/{orderId}")
    public String ship(@PathVariable("orderId") int orderId, HttpServletResponse response) {
        try {
            int shipmentId = shippingService.ship(orderId, CurrentUser.employeeId());
            alert(response, "Заказ отгружен - документ " + shippingService.getShipment(shipmentId).number());
        } catch (WmsException e) {
            log.warn("Отгрузка отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return REDIRECT;
    }

    /** Товарная накладная ТОРГ-12 на отгрузку. */
    @GetMapping("/torg_12/{id}")
    public String torg12(@PathVariable("id") int id, Model model) {
        ShippingService.ShipmentView shipment = shippingService.getShipment(id);
        String company = wmsProperties.getCompany().getName();
        String customer = shipment.customer() + ", код " + shipment.customerCode()
                + (shipment.address().isBlank() ? "" : ", " + shipment.address());

        HashMap<String, String> doc = new HashMap<>();
        doc.put("number", shipment.number());
        doc.put("shipper", company);
        doc.put("supplier", company);
        doc.put("consignee", customer);
        doc.put("payer", customer);
        doc.put("basis", "Заказ клиента № " + shipment.orderNumber());

        List<HashMap<String, String>> lines = new ArrayList<>();
        int totalQuantity = 0;
        double totalWeight = 0;
        for (ShippingService.LineView line : shipment.lines()) {
            HashMap<String, String> row = new HashMap<>();
            row.put("code", line.productCode());
            row.put("name", line.productName());
            row.put("qntFact", String.valueOf(line.quantity()));
            row.put("weightFact", String.format(java.util.Locale.ROOT, "%.2f", line.weight()));
            lines.add(row);
            totalQuantity += line.quantity();
            totalWeight += line.weight();
        }
        HashMap<String, String> itog = new HashMap<>();
        itog.put("sunQnt", String.valueOf(totalQuantity));
        itog.put("sumWeight", String.format(java.util.Locale.ROOT, "%.2f", totalWeight));

        model.addAttribute("doc", doc);
        model.addAttribute("lines", lines);
        model.addAttribute("orderItog", itog);
        model.addAttribute("printDate", new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date()));
        return "torg-12";
    }

    private void alert(HttpServletResponse response, String message) {
        response.addCookie(new AddCookie("alertMessage", message).getCookie());
    }
}
