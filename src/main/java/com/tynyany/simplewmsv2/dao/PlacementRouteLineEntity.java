package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="placement_route_line")
public class PlacementRouteLineEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int pRouteLineId;

    int pRouteId;
    int qnt;
    int locationId;
    int batchId;
    int productId;
    Boolean complete ;
    Boolean del;
}
