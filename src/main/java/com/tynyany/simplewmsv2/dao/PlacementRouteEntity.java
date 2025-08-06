package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="placement_route")
public class PlacementRouteEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int pRouteId;
    Timestamp dtCreate = new Timestamp(System.currentTimeMillis());
    Timestamp dtStart;
    Timestamp dtStop;
    int status = 0;
    int employerId = 0;
    Boolean del = false;
}
