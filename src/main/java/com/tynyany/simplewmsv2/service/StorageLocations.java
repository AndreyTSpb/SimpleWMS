package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.entity.Location;
import com.tynyany.simplewmsv2.entity.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ячейки и зоны хранения для выбора в интерфейсе: без удалённых и без системных
 * (ячейки приёмки/отгрузки и их зоны).
 */
@Component
@RequiredArgsConstructor
public class StorageLocations {

    private final LocationService locationService;
    private final ZoneService zoneService;
    private final WmsProperties wmsProperties;

    public List<Location> cells() {
        Set<Integer> systemZones = systemZones();
        WmsProperties.Locations system = wmsProperties.getLocations();
        return locationService.getAllLocation().stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDel()))
                .filter(l -> l.getLocationID() != system.getReceivingId() && l.getLocationID() != system.getShippingId())
                .filter(l -> !systemZones.contains(l.getZoneID()))
                .sorted(Comparator.comparing(l -> l.getLocationCode() == null ? "" : l.getLocationCode()))
                .toList();
    }

    public List<Zone> zones() {
        Set<Integer> systemZones = systemZones();
        return zoneService.getAll().stream()
                .filter(z -> !Boolean.TRUE.equals(z.getDel()))
                .filter(z -> !systemZones.contains(z.getZoneID()))
                .toList();
    }

    private Set<Integer> systemZones() {
        Set<Integer> zones = new HashSet<>();
        WmsProperties.Locations system = wmsProperties.getLocations();
        for (int id : new int[]{system.getReceivingId(), system.getShippingId()}) {
            try {
                zones.add(locationService.getLocationByID(id).getZoneID());
            } catch (RuntimeException e) {
                // системная ячейка не создана — её зону не исключаем
            }
        }
        return zones;
    }
}
