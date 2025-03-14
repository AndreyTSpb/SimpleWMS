package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Role {
    int roleID;
    String name;
    String note;
    int del;

    public Role(int i, String name, String note, int del) {
        this.roleID = i;
        this.name = name;
        this.note = note;
        this.del = del;
    }
}
