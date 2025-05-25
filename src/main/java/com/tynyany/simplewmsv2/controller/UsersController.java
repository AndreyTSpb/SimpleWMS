package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.dao.UserRepository;
import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.entity.UserString;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import com.tynyany.simplewmsv2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    final private String baseUrl = "users";
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleService roleService;

    private final EmployeeService employeeService;

    @GetMapping
    public String index(Model model) {


        List<UserString> user_list = userService.getAllUserString();

        List<Employee> employees = employeeService.getAllEmployee();


        model.addAttribute("title", "Список пользователей");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("userList", user_list);
        model.addAttribute("employeeList1", employees);
        model.addAttribute("user", new UserEntity());

        return "users";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        userService.addUser(user);
        return "redirect:/" + baseUrl;
    }

    @PostMapping("/update")
    public String editSubmit(@ModelAttribute User user) {
        if (!userRepository.existsById(user.getUserID()))
            throw new UserNotFoundException("User not found: id = " + user.getUserID());
        userService.updateUser(user);
        return "redirect:/"+baseUrl;
    }

    /**
     * Удаление пользователя
     * @param userID - идентификатор пользователя
     * @return
     */
    @RequestMapping("/del")
    public String delUser(@RequestParam(value = "userid", required = true) int userID){
        if (!userRepository.existsById(userID))
            throw new UserNotFoundException("User not found: id = " + userID);
        User user = userService.getUserByID(userID);
        userService.delUser(user);
        return "redirect:/" + baseUrl;
    }
}
