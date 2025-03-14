package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Role;

import java.util.List;

public interface RoleService {
    Role getRoleByID(int roleID);
    List<Role> getAllRole();
    void addRole(Role role);

    void updateRole(Role role);

    void delRole(Role role);
}
