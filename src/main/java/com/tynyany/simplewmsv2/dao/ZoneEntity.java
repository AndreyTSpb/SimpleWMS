package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="zones")
public class ZoneEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int zoneId;
    private String zoneName;
    private String description;
    private String code;
    private Boolean del;
}
