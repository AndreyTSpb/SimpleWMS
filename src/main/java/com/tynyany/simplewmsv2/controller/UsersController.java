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


        List<User> user_list = userService.getAllUser();

        List<Employee> employees = employeeService.getAllEmployee();
        /*
         * [
         * Employee(employeeID=1, employeeName=Тыняный Андрей, tabNum=00001, del=1, roleID=7),
         * Employee(employeeID=2, employeeName=Тыняный Андрей, tabNum=00001, del=0, roleID=7),
         * Employee(employeeID=3, employeeName=Шароватов Станислав, tabNum=00002, del=0, roleID=6),
         * Employee(employeeID=4, employeeName=Рыкалина Елена, tabNum=00003, del=0, roleID=3),
         * Employee(employeeID=5, employeeName=Петрова Надежда, tabNum=00004, del=0, roleID=4),
         * Employee(employeeID=6, employeeName=Телепнев Николай, tabNum=00005, del=0, roleID=2),
         * Employee(employeeID=7, employeeName=Мулянов Андрей, tabNum=00006, del=0, roleID=2),
         * Employee(employeeID=8, employeeName=Путинцев Михаил, tabNum=00007, del=0, roleID=1),
         * Employee(employeeID=9, employeeName=Волошинв Галина, tabNum=00008, del=0, roleID=5),
         * Employee(employeeID=10, employeeName=Осипова Светлана, tabNum=00009, del=0, roleID=1),
         * Employee(employeeID=11, employeeName=Бондоренко Сергей, tabNum=05103, del=0, roleID=2)
         * ]
         */

        List<Role> roles = roleService.getAllRole();

         HashMap<Integer, Employee> aas = arrayEmployee(employees);
        System.out.println(aas);
         for (User user : user_list) {
             System.out.println(user.getUserID());
             System.out.println(aas.get(user.getEmployeeID()).getEmployeeID());
         }

        model.addAttribute("title", "Список пользователей");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("userList", user_list);
        model.addAttribute("employeeList", aas);
        model.addAttribute("roleList", roles);
        model.addAttribute("user", new UserEntity());

        return "users";
    }

    private HashMap<Integer, Employee> arrayEmployee(List<Employee> employeeList){
        HashMap<Integer, Employee> arr = new HashMap<>();
        for (Employee employee : employeeList){
            arr.put(employee.getEmployeeID(), employee);
        }
        return arr;
    }

    private Employee[] employees(List<Employee> employeeList){
        Employee[] arr = new Employee[employeeList.size()];
        for (Employee employee : employeeList){
            arr[employee.getEmployeeID()] =  employee;
        }
        return arr;
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
