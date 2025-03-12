package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SuppliersController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Список поставщиков");
        model.addAttribute("supplierList", supplierList());
        return "suppliers";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("title", "Добавить поставщика");
        return "add_user";
    }

    public static Supplier[] supplierList(){
        Supplier[] supplierList = new Supplier[3];
        supplierList[0] = new Supplier(0, "ООО РиК", "П000009", "Остап", "+7(812)1234567", "ric@mail.ru", false);
        supplierList[1] = new Supplier(1, "Святогорский бумажный комбинат", "П000019", "Людмила Леонидовна", "+7(812)1234567", "sv@mail.ru", true);
        supplierList[2] = new Supplier(2, "Монди", "П000021", "Андрей Андреевич", "+7(812)1234567", "mondi@mail.ru", false);
        return supplierList;
    }
}
