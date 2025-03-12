package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Category {
    int categoryID;
    String categoryName;
    String description;
    Boolean del;

    public  Category(int categoryID, String categoryName, String description, Boolean del){

        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.description = description;
        this.del = del;
    }
}
