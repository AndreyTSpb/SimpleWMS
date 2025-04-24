package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="supplier")
public class SupplierEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="supplierid")
    private int supplierID;
    private String supplierName;
    private String supplierCode;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private Boolean del = false;
}
