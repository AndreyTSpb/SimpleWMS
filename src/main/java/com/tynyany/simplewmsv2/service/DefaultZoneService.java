package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.ZoneEntity;
import com.tynyany.simplewmsv2.dao.ZoneRepository;
import com.tynyany.simplewmsv2.entity.Zone;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultZoneService implements ZoneService {

    private final ZoneRepository zoneRepository;

    @Override
    public Zone getByID(int zoneId) {
        ZoneEntity zoneEntity = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new UserNotFoundException("Zone not found:id =" + zoneId));

        return new Zone(zoneEntity.getZoneId(),zoneEntity.getZoneName(), zoneEntity.getCode(),zoneEntity.getDescription(),zoneEntity.getDel());
    }

    @Override
    public List<Zone> getAll() {
        Iterable<ZoneEntity> zoneEntities = zoneRepository.findAll();
        ArrayList<Zone> zones = new ArrayList<>();
        for (ZoneEntity zoneEntity : zoneEntities) {
            zones.add(new Zone(zoneEntity.getZoneId(),zoneEntity.getZoneName(), zoneEntity.getCode(),zoneEntity.getDescription(),zoneEntity.getDel()));
        }
        return zones;
    }

    @Override
    public void add(Zone zone) {
        ZoneEntity  zoneEntity = new ZoneEntity(
                0,
                zone.getZoneName(),
                zone.getDescription(),
                zone.getCode(),
                false
        );
        zoneRepository.save(zoneEntity);
    }

    @Override
    public void update(Zone zone) {
        ZoneEntity  zoneEntity = new ZoneEntity(
                zone.getZoneID(),
                zone.getZoneName(),
                zone.getDescription(),
                zone.getCode(),
                zone.getDel()
        );
        zoneRepository.save(zoneEntity);
    }

    @Override
    public void del(Zone zone) {
        ZoneEntity  zoneEntity = new ZoneEntity(
                zone.getZoneID(),
                zone.getZoneName(),
                zone.getDescription(),
                zone.getCode(),
                true
        );
        zoneRepository.save(zoneEntity);
    }
}
