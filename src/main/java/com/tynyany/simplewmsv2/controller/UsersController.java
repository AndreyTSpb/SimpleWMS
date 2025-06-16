package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.EmployeeEntity;
import com.tynyany.simplewmsv2.dao.RoleEntity;
import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.models.DelCookie;
import com.tynyany.simplewmsv2.repository.EmployeeRepository;
import com.tynyany.simplewmsv2.repository.UserRepository;
import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.entity.UserString;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import com.tynyany.simplewmsv2.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public String index(@RequestParam(required = false, defaultValue = "") String filter,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @PageableDefault(sort = { "userID" }, direction = Sort.Direction.ASC) Pageable pageable) {

        Page<UserEntity> page;
        String filterString = "";

        if (filter != null && !filter.isEmpty()) {
            filterString = "&filter="+filter;
            page = userService.getAllUserPageableWithFilter(pageable, "%"+filter+"%");
        } else {
            page = userService.getAllUserPageable(pageable);
        }

        int currentPage = pageable.getPageNumber();

        if(currentPage < 0) currentPage = 0;

        int totalPages = page.getTotalPages()-1;
        if(totalPages < 0) totalPages = 0;

        String updateMessage = null;
        String cookieName = "alertMessage";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    // Кука с этим именем есть
                    updateMessage = cookie.getValue().replace('_', ' ');
                    response.addCookie(new DelCookie(cookieName).getCookie());
                }
            }
        }

        List<Employee> employees = employeeService.getAllEmployee();


        model.addAttribute("title", "Список пользователей");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("userList", userStringList(page));
        model.addAttribute("employeeList1", employees);
        model.addAttribute("user", new UserEntity());

        // сообщение об обновлении данных
        model.addAttribute("updateMessage", updateMessage);

        // пагинация
        model.addAttribute("totalPage", totalPages);
        model.addAttribute("filter", filterString);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("previewPage", currentPage-1);
        model.addAttribute("nextPage", currentPage+1);

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

    /**
     *  Собираем массив строчек для таблицы пользователей
     * @param iterable
     * @return List
     */
    private List<UserString> userStringList(Page<UserEntity> iterable) {
        List<UserString> list = new ArrayList<>();
        for(UserEntity userEntity : iterable){
            System.out.println(userEntity);
            //через айди получаем фио сотрудника привязанного
            int employeeID = userEntity.getEmployeeID();
            int roleId = 0;
            String employeeName = "";
            String roleName = "";

            if(employeeID != 0){
                Employee employee = employeeService.getEmployeeByID(employeeID);
                employeeName = employee.getEmployeeName();
                if(employee.getRoleID() != 0){
                    roleId = employee.getRoleID();
                    Role role = roleService.getRoleByID(roleId);
                    roleName = role.getRoleName();
                }
            }

            list.add(new UserString(
                    userEntity.getUserID(),
                    userEntity.getLogin(),
                    userEntity.getDel(),
                    employeeID,
                    employeeName,
                    roleId,
                    roleName
            ));
        }
        return list;
    }
}
