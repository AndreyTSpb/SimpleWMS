package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;

import java.util.List;

public interface EmployeeService {
    Employee getEmployeeByID(int employeeID);

    List<Employee> getAllEmployee();
    void addEmployee(Employee employee);

    void updateEmployee(Employee employee);

    void delEmployee(Employee employee);
}
