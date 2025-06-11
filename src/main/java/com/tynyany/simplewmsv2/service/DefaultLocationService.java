package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.LocationEntity;
import com.tynyany.simplewmsv2.repository.LocationRepository;
import com.tynyany.simplewmsv2.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultLocationService implements LocationService {

    final LocationRepository locationRepository;
    final ZoneService zoneService;

    @Override
    public Location getLocationByID(int locationID) {
        return null;
    }

    @Override
    public List<Location> getAllLocation() {
        Iterable<LocationEntity> locationEntities = locationRepository.findAll();
        ArrayList<Location> locations = new ArrayList<>();
        for (LocationEntity locationEntity : locationEntities) {
            locations.add(
                    new Location(
                            locationEntity.getLocationID(),
                            locationEntity.getLocationCode(),
                            locationEntity.getRow(),
                            locationEntity.getX(),
                            locationEntity.getY(),
                            locationEntity.getZ(),
                            locationEntity.getCapacity(),
                            locationEntity.getAvailable() != 0,
                            locationEntity.getDel() != 0,
                            locationEntity.getZoneID()
                    )
            );
        }
        return locations;
    }

    /**
     * Выборка всех мест для таблицы с пагинацией
     *
     * @return Page<LocationEntity>
     */
    @Override
    public Page<LocationEntity> getAllLocationForTable(Pageable pageable) {
        Page<LocationEntity> page = locationRepository.findAllBy(pageable);
        System.out.println(page.getTotalElements());
        //
        return page;
    }

    @Override
    public Page<LocationEntity> getAllLocationForTableWithFilter(String filter, Pageable pageable) {
        Page<LocationEntity> page = locationRepository.findByLocationCodeIsLikeIgnoreCase("%"+filter+"%", pageable);
        System.out.println(page.getTotalElements());
        return page;
    }

    @Override
    public void addLocation(Location location) {
        LocationEntity entity = new LocationEntity(
                0,
                location.getLocationCode(),
                location.getRow(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getCapacity(),
                (location.getAvailable()) ? 1:0,
                0,
                location.getZoneID()
        );
        locationRepository.save(entity);
    }

    @Override
    public void updateLocation(Location location) {
        LocationEntity entity = new LocationEntity(
                location.getLocationID(),
                location.getLocationCode(),
                location.getRow(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getCapacity(),
                (location.getAvailable() != null) ? 1:0,
                (location.getDel() != null) ? 1:0,
                location.getZoneID()
        );
        locationRepository.save(entity);
    }

    @Override
    public void delLocation(int locationID) {
        LocationEntity entity = locationRepository.findById(locationID).orElse(null);
        assert entity != null;
        entity.setDel(1);
        locationRepository.save(entity);
    }
}
