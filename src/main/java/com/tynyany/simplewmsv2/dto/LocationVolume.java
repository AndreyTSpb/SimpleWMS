package com.tynyany.simplewmsv2.dto;

/**
 * Объём (литры) по ячейке — результат агрегирующих запросов.
 */
public interface LocationVolume {
    Integer getLocationId();
    Double getVolume();
}
