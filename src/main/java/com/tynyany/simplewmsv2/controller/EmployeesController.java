package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.EmployeeRepository;
import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeesController {

    private final EmployeeRepository employeeRepository;
    private String baseUrl = "employees";
    private final EmployeeService employeeService;
    private final RoleService roleService;

    @GetMapping
    public String index(Model model) {
        List<Role> roles = roleService.getAllRole();

        model.addAttribute("title", "Список сотрудников");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("employeesList", employeeService.getAllEmployee());
        model.addAttribute("rolesList", roles);
        return "employee";
    }


    @RequestMapping("/add")
    public String addEmployee(@ModelAttribute Employee employee) {
        employeeService.addEmployee(employee);
        return "redirect:/" + baseUrl;
    }

    @RequestMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee) {
        if(!employeeRepository.existsById(employee.getEmployeeID()))
            throw new UserNotFoundException("Employee not found: id = " + employee.getEmployeeID());
        employeeService.addEmployee(employee);
        return "redirect:/" + baseUrl;
    }

    @RequestMapping("/del")
    public String delEmployee(@RequestParam(value = "employeeId", required = true) int employeeId) {
        if(!employeeRepository.existsById(employeeId))
            throw new UserNotFoundException("Employee not found: id = " + employeeId);

        Employee employee = employeeService.getEmployeeByID(employeeId);
        employeeService.delEmployee(employee);
        return "redirect:/" + baseUrl;
    }


}
