package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeesController {

    private String baseUrl = "employees";
    private final EmployeeService employeeService;
    private final RoleService roleService;

    @GetMapping
    public String index(Model model) {
        List<Role> roles = roleService.getAllRole();

        model.addAttribute("title", "Список сотрудников");
        model.addAttribute("baseUel", this.baseUrl);
        model.addAttribute("employeesList", employeeService.getAllEmployee());
        model.addAttribute("rolesList", roles);
        return "employee";
    }


    @RequestMapping("/add")
    @ResponseBody
    public String addEmployee(){
        return "hi";
    }
}
