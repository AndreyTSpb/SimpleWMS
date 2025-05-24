package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class UserString {
    int userID;
    String login;
    Boolean del;
    int employeeID;
    String employeeName;
    int roleID;
    String roleName;

    public UserString(int userID, String login, Boolean del, int employeeID, String employeeName, int roleID, String roleName) {
        this.userID = userID;
        this.login = login;
        this.del = del;
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.roleID = roleID;
        this.roleName = roleName;
    }
}
