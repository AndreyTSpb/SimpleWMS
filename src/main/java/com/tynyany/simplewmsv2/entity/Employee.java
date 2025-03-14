package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class Employee {
    int employeeID;
    String employeeName;
    String tabNum;
    int del;
    int roleID;

    public Employee(int employeeID, String employeeName, String tabNum, int roleID, int del){

        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.tabNum = tabNum;
        this.roleID = roleID;
        this.del = del;
    }
}

