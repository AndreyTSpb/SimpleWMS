package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//customer_list
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomersController {
    @GetMapping
    public String index(Model model) {

        model.addAttribute("title", "Список клиентов");
        model.addAttribute("customerList", customerList());

        return "customers";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("title", "Добавить поставщика");
        return "add_user";
    }

    public static Customer[] customerList(){
        Customer[] customerList = new Customer[5];
        customerList[0] = new Customer(0, "КЛ15062", "М-Книга", "+7(812)1234567", "mbook@mail.ru", "Спб, Новгородская ул., д.10", false, "10:00", "18:00");
        customerList[1] = new Customer(0, "КЛ15002", "Энергия", "+7(812)1234567", "energy@mail.ru", "Спб, Малая Бронная ул., д.22", false, "09:00", "20:00");
        customerList[2] = new Customer(0, "КЛ15122", "Графика М", "+7(812)1234567", "graf-m@mail.ru", "Спб, Путиловская ул., д.1", false, "10:00", "22:00");
        customerList[3] = new Customer(0, "КЛ15210", "Граффит", "+7(812)1234567", "grafit@mail.ru", "Спб, Кировская ул., д.12", false, "10:00", "20:00");
        customerList[4] = new Customer(0, "КЛ15451", "КанцТорг", "+7(812)1234567", "kanctop@mail.ru", "Спб, Зайцева ул., д.20", false, "10:00", "18:00");
        return customerList;
    }
}
