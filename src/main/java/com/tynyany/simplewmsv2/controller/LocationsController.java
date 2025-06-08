package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.dao.LocationRepository;
import com.tynyany.simplewmsv2.dao.ZoneRepository;
import com.tynyany.simplewmsv2.entity.Location;
import com.tynyany.simplewmsv2.entity.Zone;
import com.tynyany.simplewmsv2.service.LocationService;
import com.tynyany.simplewmsv2.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationsController {
    final private String baseUrl = "locations";
    private final LocationService locationService;
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final ZoneService zoneService;

    @GetMapping
    public String index(Model model) {
        List<Zone> zones = ZonesController.zonesList();
        List<Location> locationList = locationService.getAllLocation();


        model.addAttribute("title", "Список мест хранения");
        model.addAttribute("locationList", locationList);
        model.addAttribute("zoneList", zones);
        model.addAttribute("locationEntity", new LocationEntity());
        model.addAttribute("baseUrl", baseUrl);
        return "locations";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute LocationEntity locationEntity) {
        System.out.println(locationEntity);
        Zone zone = zoneService.getByID(locationEntity.getZoneID());

        String row = (locationEntity.getRow().length() > 1)? locationEntity.getRow():"0" + locationEntity.getRow();
        String x = (locationEntity.getX() > 9)? locationEntity.getX() + "": "0"+locationEntity.getX();
        String y = String.valueOf(locationEntity.getY());
        String z = String.valueOf(locationEntity.getY());
        String locationCode = zone.getCode() + "-" + row + "-" + x + "-" + y + "-" + z;

        locationService.addLocation(new Location(
                locationEntity.getLocationID(),
                locationCode,
                row,
                locationEntity.getX(),
                locationEntity.getY(),
                locationEntity.getZ(),
                locationEntity.getCapacity(),
                locationEntity.getAvailable() == 1,
                false,
                locationEntity.getZoneID()
        ));
        return "redirect:/" + baseUrl;
    }

    public static Location[] locationsList(){
        Location[] locationsList = new Location[20];
        locationsList[0] = new Location(0, "not-00-01-1-1", "0", 1, 1, 1, 1F, true,true, 0);
        locationsList[1] = new Location(1, "SC1-01-02-2-1", "1", 1, 2, 1, 1F, true,false, 2);
        locationsList[2] = new Location(2, "SC1-01-03-3-1", "1", 1, 3, 1, 1F, true,false, 2);
        locationsList[3] = new Location(3, "SC1-01-02-1-1", "1", 2, 1, 1, 1F, true,false, 2);
        locationsList[4] = new Location(4, "SC1-01-02-2-1", "1", 2, 2, 1, 1F, true,false, 2);
        locationsList[5] = new Location(5, "SC1-01-02-3-1", "1", 2, 3, 1, 1F, true,false, 2);
        locationsList[6] = new Location(6, "SC1-01-02-4-1", "1", 2, 4, 1, 1F, true,false, 2);
        locationsList[7] = new Location(7, "SC1-01-03-1-1", "1", 3, 1, 1, 1F, true,false, 2);
        locationsList[8] = new Location(8, "SC1-01-03-2-1", "1", 3, 2, 1, 1F, true,false, 2);
        locationsList[9] = new Location(9, "SC1-01-03-3-1", "1", 3, 3, 1, 1F, true,false, 2);
        locationsList[10] = new Location(10, "SC2-02-04-1-1", "2", 4, 1, 1, 1F, true,false, 3);
        locationsList[11] = new Location(11, "SC2-02-04-3-1", "2", 4, 3, 1, 1F, true,false, 3);
        locationsList[12] = new Location(12, "SC2-02-04-4-1", "2", 4, 4, 1, 1F, true,false, 3);
        locationsList[13] = new Location(13, "SC2-02-05-1-1", "2", 5, 1, 1, 1F, true,false, 3);
        locationsList[14] = new Location(14, "SC2-02-05-2-1", "2", 5, 2, 1, 0.5F, true,false, 3);
        locationsList[15] = new Location(15, "SC2-02-05-2-2", "2", 5, 2, 2, 0.5F, true,false, 3);
        locationsList[16] = new Location(16, "SC2-02-05-4-1", "2", 5, 4, 1, 1F, true,false, 3);
        locationsList[17] = new Location(17, "SC2-02-06-1-1", "2", 6, 1, 1, 1F, true,false, 3);
        locationsList[18] = new Location(18, "SC2-02-06-2-1", "2", 6, 2, 1, 1F, true,false, 3);
        locationsList[19] = new Location(19, "SC2-02-06-3-1", "2", 6, 3, 1, 1F, true,false, 3);
        return locationsList;
    }

}
