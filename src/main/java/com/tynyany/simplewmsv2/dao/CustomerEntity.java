package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="customers")
public class CustomerEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int customerId;
    private String customerCode;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private String workingTimeStr;
    private String workingTimeEnd;
    private Boolean del;
}
