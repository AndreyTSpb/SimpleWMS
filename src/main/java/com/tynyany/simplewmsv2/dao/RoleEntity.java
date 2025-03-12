package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="role")
public class RoleEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int roleID;
    private String roleName;
}
