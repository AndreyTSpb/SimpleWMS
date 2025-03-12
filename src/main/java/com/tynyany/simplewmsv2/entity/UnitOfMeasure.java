package com.tynyany.simplewmsv2.entity;

import lombok.Value;


@Value
public class UnitOfMeasure {
    int unitOfMeasureID;
    String unitName;
    String code;
    Boolean del;

    public UnitOfMeasure(int unitOfMeasureID, String unitName, String code, Boolean del){
        this.unitOfMeasureID = unitOfMeasureID;
        this.unitName = unitName;
        this.code = code;

        this.del = del;
    }
}
