package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@JsonAutoDetect
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
