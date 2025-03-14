package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.dao.UserRepository;
import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import com.tynyany.simplewmsv2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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


        List<User> user_list = userService.getAllUser();

        List<Employee> employees = employeeService.getAllEmployee();


        List<Role> roles = roleService.getAllRole();


        model.addAttribute("title", "Список пользователей");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("userList", user_list);
        model.addAttribute("employeeList", employees);
        model.addAttribute("roleList", roles);
        model.addAttribute("user", new UserEntity());

        return "users";
    }

    public User[] userList(){
        User[] user_list = new User[10];

        user_list[0] = new User(0, "admin@mail.ru", "password", true, 1);
        user_list[1] = new User(1, "admin@mail.ru", "password", false, 1);
        user_list[2] = new User(2, "sharovatov@mail.ru", "password", false, 2);
        user_list[3] = new User(3, "rykalina@mail.ru", "password", false, 3);
        user_list[4] = new User(4, "petrova@mail.ru", "password", false, 4);
        user_list[5] = new User(5, "telepnev@mail.ru", "password", false, 5);
        user_list[6] = new User(6, "mulanov@mail.ru", "password", false, 6);
        user_list[7] = new User(7, "putincev@mail.ru", "password", false, 7);
        user_list[8] = new User(8, "voloshina@mail.ru", "password", false, 8);
        user_list[9] = new User(9, "osipova@mail.ru", "password", false, 9);
        return user_list;
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        userService.addUser(user);
        return "redirect:/"+baseUrl;
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
