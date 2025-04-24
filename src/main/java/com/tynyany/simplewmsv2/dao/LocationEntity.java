package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="location")
public class LocationEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int locationID;
    private String locationCode;
    private String row; //ряд
    private int x; // координаты в горизонтальной плоскости
    private int y; // координаты в вертикальной плоскости
    private int z; // порядковый номер полки при разделении места
    private float capacity = 1.0F;
    private int available = 1;
    private int del = 0;
    private int zoneID;
}
