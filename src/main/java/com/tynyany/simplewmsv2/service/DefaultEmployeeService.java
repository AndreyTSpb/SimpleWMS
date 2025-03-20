package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.EmployeeEntity;
import com.tynyany.simplewmsv2.dao.EmployeeRepository;
import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultEmployeeService implements EmployeeService{

    final EmployeeRepository employeeRepository;

    @Override
    public Employee getEmployeeByID(int employeeID) {
        return null;
    }

    @Override
    public List<Employee> getAllEmployee() {

        Iterable<EmployeeEntity> iterable = employeeRepository.findAll();

        ArrayList<Employee> employees = new ArrayList<>();

        for (EmployeeEntity employeeEntity : iterable){
            employees.add( new Employee(
                    employeeEntity.getEmployeeID(),
                    employeeEntity.getEmployeeName(),
                    employeeEntity.getTabNum(),
                    employeeEntity.getRoleID(),
                    employeeEntity.getDel()
            ));
        }

        return employees;
    }

    @Override
    public void addEmployee(Employee employee) {
        EmployeeEntity employeeEntity = new EmployeeEntity(
                0,
                employee.getEmployeeName(),
                employee.getTabNum(),
                employee.getDel(),
                employee.getRoleID()
        );
        employeeRepository.save(employeeEntity);
    }

    @Override
    public void updateEmployee(Employee employee) {

    }

    @Override
    public void delEmployee(Employee employee) {

    }
}
