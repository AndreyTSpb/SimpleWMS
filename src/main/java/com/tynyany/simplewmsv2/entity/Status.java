package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Status {
    int statusID;
    String name; //"В ожидании", "В процессе", "Завершена", "Отменена"

    public Status(int statusID, String name){

        this.statusID = statusID;
        this.name = name;
    }
}
