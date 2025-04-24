package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="roles")
public class RoleEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleID;
    private String roleName;
    private String note;
}
