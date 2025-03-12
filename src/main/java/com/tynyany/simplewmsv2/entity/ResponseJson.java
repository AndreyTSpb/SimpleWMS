package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class ResponseJson {
    int success;
    String message;

    public ResponseJson(int success, String message){
        this.success = success;

        this.message = message;
    }
}
