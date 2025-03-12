package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/zones")
@RequiredArgsConstructor
public class ZonesController {
    private String baseUrl = "zones";
    @GetMapping
    public String index(Model model) {
        Zone[] zoneslist = zonesList();
        model.addAttribute("title", "Список зон хранения");
        model.addAttribute("zonesList", zoneslist);
        return "zones";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("title", "Добавить секцию");
        return "add_user";
    }


    public static Zone[] zonesList(){
        Zone[] zonesList = new Zone[7];
        zonesList[0] = new Zone(0, "Не используется", "not", "Не используется", true);
        zonesList[1] = new Zone(1, "Ангар", "ANG", "Хранение бумаги", false);
        zonesList[2] = new Zone(2, "Секция 1", "SC1", "Бумажно беловая продукция", false);
        zonesList[3] = new Zone(3, "Секция 2", "SC2", "Канцелярские товары", false);
        zonesList[4] = new Zone(4, "Секция 3", "HOZ", "Хозяйственные товары и прочее", false);
        zonesList[5] = new Zone(5, "Секция 4", "BRK", "Брак и неликвид", false);
        zonesList[6] = new Zone(6, "Приемка", "INT", "Место приемки товара", false);
        return zonesList;
    }
}
