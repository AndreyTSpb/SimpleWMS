package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class ABC {
    int abcID;
    String code;
    String description;
    Boolean del;

    public  ABC(int abcID, String code, String description, Boolean del){

        this.abcID = abcID;
        this.code = code;
        this.description = description;
        this.del = del;
    }
}
