package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.RoleEntity;
import com.tynyany.simplewmsv2.repository.RoleRepository;
import com.tynyany.simplewmsv2.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultRoleService implements RoleService{

    final RoleRepository roleRepository;

    @Override
    public Role getRoleByID(int roleID) {
        RoleEntity roleEntity = roleRepository.findById(roleID).orElse(null);
        assert roleEntity != null;
        return new Role(
                roleEntity.getRoleID(),
                roleEntity.getRoleName(),
                roleEntity.getNote()
        );
    }

    @Override
    public List<Role> getAllRole() {
        Iterable<RoleEntity> roleEntities =  roleRepository.findAll();

        ArrayList<Role> roles = new ArrayList<>();
        for (RoleEntity roleEntity : roleEntities){
            roles.add(new Role(
                    roleEntity.getRoleID(),
                    roleEntity.getRoleName(),
                    roleEntity.getNote()
            ));
        }
        return roles;
    }

    @Override
    public void addRole(Role role) {
        RoleEntity roleEntity  = new RoleEntity(
                0,
                role.getRoleName(),
                role.getNote()
        );
        roleRepository.save(roleEntity);
    }

    @Override
    public void updateRole(Role role) {

    }

    @Override
    public void delRole(Role role) {

    }
}
