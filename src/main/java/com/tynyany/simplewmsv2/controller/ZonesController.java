package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.repository.ZoneRepository;
import com.tynyany.simplewmsv2.entity.Zone;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/zones")
@RequiredArgsConstructor
public class ZonesController {
    private final ZoneService zoneService;
    private final ZoneRepository zoneRepository;
    private final String baseUrl = "zones";
    @GetMapping
    public String index(Model model) {
        List<Zone> zoneslist = zoneService.getAll();
        model.addAttribute("title", "Список зон хранения");
        model.addAttribute("zonesList", zoneslist);
        model.addAttribute("baseUrl", baseUrl);
        return "zones";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Zone zone) {
        //Optional<ZoneEntity> zoneEntity = zoneRepository.findByZoneName(zone.getZoneName());
        //if(zoneEntity.isEmpty()) {
            zoneService.add(zone);
        //}
        return "redirect:/" + baseUrl;
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Zone zone) {
        if(!zoneRepository.existsById(zone.getZoneID()))
            throw new UserNotFoundException("Zone not found: id = " + zone.getZoneID());
        zoneService.update(zone);
        return "redirect:/" + baseUrl;
    }

    @PostMapping("/del")
    public String delete(@ModelAttribute Zone zone) {
        if(!zoneRepository.existsById(zone.getZoneID()))
            throw new UserNotFoundException("Zone not found: id = " + zone.getZoneID());
        Zone zone1  = zoneService.getByID(zone.getZoneID());
        zoneService.del(zone1);
        return "redirect:/" + baseUrl;
    }


    public static List<Zone> zonesList(){
        List<Zone> zonesList = new ArrayList<>();

        zonesList.add(new Zone(1, "Не используется", "not", "Не используется", true));
        zonesList.add(new Zone(2, "Ангар", "ANG", "Хранение бумаги", false));
        zonesList.add(new Zone(3, "Секция 1", "SC1", "Бумажно беловая продукция", false));
        zonesList.add(new Zone(4, "Секция 2", "SC2", "Канцелярские товары", false));
        zonesList.add(new Zone(5, "Секция 3", "HOZ", "Хозяйственные товары и прочее", false));
        zonesList.add(new Zone(6, "Секция 4", "BRK", "Брак и неликвид", false));
        zonesList.add(new Zone(7, "Приемка", "INT", "Место приемки товара", false));
        return zonesList;
    }
}
