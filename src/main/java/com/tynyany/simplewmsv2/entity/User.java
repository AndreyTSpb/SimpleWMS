package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class User {
    int userID;
    String login;
    String pass;
    Boolean del;
    int employeeID;

    public User(int userID, String login, String pass, Boolean del, int employeeID){
        this.userID = userID;
        this.login = login;
        this.pass = pass;
        this.del = del;
        this.employeeID = employeeID;
    }
}
