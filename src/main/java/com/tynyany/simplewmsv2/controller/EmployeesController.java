package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeesController {

    private String baseUrl = "employees";

    @GetMapping
    public String index(Model model) {

        Employee[] employees = employeesList();
        Role[] rolesList = RolesController.RoleList();

        model.addAttribute("title", "Список сотрудников");
        model.addAttribute("baseUel", this.baseUrl);
        model.addAttribute("employeesList", employees);
        model.addAttribute("rolesList", rolesList);
        return "employee";
    }

    public static Employee[] employeesList(){
        Employee[] employees = new Employee[10];

        employees[0] = new Employee(0, "Тыняный Андрей", "00001", 7, true);
        employees[1] = new Employee(1, "Тыняный Андрей", "00001", 7, false);
        employees[2] = new Employee(2, "Шароватов Станислав", "00002", 6, false);
        employees[3] = new Employee(3, "Рыкалина Елена", "00003", 3, false);
        employees[4] = new Employee(4, "Петрова Надежда", "00004", 4, false);
        employees[5] = new Employee(5, "Телепнев Николай", "00005", 2, false);
        employees[6] = new Employee(6, "Мулянов Андрей", "00006", 2, false);
        employees[7] = new Employee(7, "Путинцев Михаил", "00007", 1, false);
        employees[8] = new Employee(8, "Волошинв Галина", "00008", 5, false);
        employees[9] = new Employee(9, "Осипова Светлана", "00009", 1, false);
        return employees;
    }

    public static List<Employee> employeeDropdownList(){
        // 1) полный вариант перебора
        //        for (Employee employee: employeesList()) {
        //            options.add(employee);
        //        }
        // 2) Закидываем все и сразу в переменную
        //        options.addAll(Arrays.asList(employeesList()));
        // 3) Напрямую в одну строку
        //        List<Employee> options = new ArrayList<Employee>(Arrays.asList(employeesList()));
        return new ArrayList<Employee>(Arrays.asList(employeesList()));
    }
}
