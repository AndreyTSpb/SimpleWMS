package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.domain.OrderStatus;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.UpdateMessageAlert;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.service.OrderService;
import com.tynyany.simplewmsv2.service.PickingService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Заказы клиентов: список, создание, карточка, резервирование, передача в сборку, отмена.
 */
@Slf4j
@Controller
@RequestMapping("/order_list")
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;
    private final PickingService pickingService;
    private final ShippingService shippingService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public String index(@RequestParam(value = "status", required = false) Integer status,
                        Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("title", "Заказы клиентов");
        model.addAttribute("orders", orderService.getOrders(status));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("statusFilter", status);
        model.addAttribute("customers", StreamSupport.stream(customerRepository.findAll().spliterator(), false)
                .filter(c -> !Boolean.TRUE.equals(c.getDel()))
                .sorted(Comparator.comparing(c -> c.getCustomerName() == null ? "" : c.getCustomerName()))
                .toList());
        addAlert(model, request, response);
        return "order_list";
    }

    @PostMapping("/create")
    public String create(@RequestParam("customerId") int customerId,
                         @RequestParam(value = "documentNumber", defaultValue = "") String documentNumber,
                         @RequestParam(value = "shipDate", defaultValue = "") String shipDate,
                         @RequestParam(value = "note", defaultValue = "") String note,
                         HttpServletResponse response) {
        try {
            int id = orderService.create(customerId, documentNumber, shipDate, note);
            alert(response, "Заказ создан, добавьте строки и зарезервируйте товар");
            return "redirect:/order_list/view/" + id;
        } catch (WmsException e) {
            alert(response, e.getMessage());
            return "redirect:/order_list";
        }
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") int id, Model model, HttpServletRequest request, HttpServletResponse response) {
        OrderService.OrderView order = orderService.getOrder(id);
        model.addAttribute("title", "Заказ " + order.documentNumber());
        model.addAttribute("order", order);
        model.addAttribute("pickingRouteId", pickingService.findRouteIdByOrder(id).orElse(0));
        model.addAttribute("shipmentId", shippingService.findShipmentIdByOrder(id).orElse(0));
        List<com.tynyany.simplewmsv2.dao.ProductEntity> products = new ArrayList<>();
        productRepository.findAll().forEach(p -> {
            if (!Boolean.TRUE.equals(p.getDel())) products.add(p);
        });
        products.sort(Comparator.comparing(p -> p.getProductName() == null ? "" : p.getProductName()));
        model.addAttribute("products", products);
        addAlert(model, request, response);
        return "order_view";
    }

    @PostMapping("/{id}/add_line")
    public String addLine(@PathVariable("id") int id, @RequestParam("productId") int productId,
                          @RequestParam("quantity") int quantity, HttpServletResponse response) {
        run(response, () -> orderService.addLine(id, productId, quantity), "Строка добавлена");
        return "redirect:/order_list/view/" + id;
    }

    @PostMapping("/{id}/remove_line")
    public String removeLine(@PathVariable("id") int id, @RequestParam("lineId") int lineId, HttpServletResponse response) {
        run(response, () -> orderService.removeLine(lineId), "Строка удалена");
        return "redirect:/order_list/view/" + id;
    }

    @PostMapping("/{id}/reserve")
    public String reserve(@PathVariable("id") int id, HttpServletResponse response) {
        try {
            OrderService.ReserveResult result = orderService.reserve(id);
            String message = "Зарезервировано " + result.reservedNow() + " шт., статус: " + result.status();
            if (result.shortage() > 0) {
                message += ". Не хватает " + result.shortage() + " шт.";
            }
            alert(response, message);
        } catch (WmsException e) {
            log.warn("Резервирование отклонено: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return "redirect:/order_list/view/" + id;
    }

    @PostMapping("/{id}/to_picking")
    public String toPicking(@PathVariable("id") int id, HttpServletResponse response) {
        try {
            int routeId = pickingService.createRoute(id);
            alert(response, "Заказ передан в сборку - маршрут № " + routeId);
        } catch (WmsException e) {
            log.warn("Передача в сборку отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return "redirect:/order_list/view/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") int id, HttpServletResponse response) {
        run(response, () -> orderService.cancel(id), "Заказ отменён, резерв снят");
        return "redirect:/order_list/view/" + id;
    }

    private void run(HttpServletResponse response, Runnable action, String successMessage) {
        try {
            action.run();
            alert(response, successMessage);
        } catch (WmsException e) {
            log.warn("Операция с заказом отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
    }

    private void addAlert(Model model, HttpServletRequest request, HttpServletResponse response) {
        UpdateMessageAlert alert = new UpdateMessageAlert(request, response);
        if (alert.updateMessage != null) {
            model.addAttribute("updateMessage", alert.updateMessage);
        }
    }

    private void alert(HttpServletResponse response, String message) {
        response.addCookie(new AddCookie("alertMessage", message).getCookie());
    }
}
