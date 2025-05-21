package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Role {
    int roleID;
    String roleName;
    String note;


    public Role(int i, String name, String note) {
        this.roleID = i;
        this.roleName = name;
        this.note = note;
    }
}
