package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolesController {

    private final RoleService roleService;

    private String baseUrl = "roles";

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Список ролей");
        model.addAttribute("baseUel", this.baseUrl);
        model.addAttribute("roleList", roleService.getAllRole());
        return "roles_list";
    }

    @RequestMapping("/add")
    @ResponseBody
    public String addRole(){

//        for(Role role : RoleList()){
//            roleService.addRole(role);
//        }
        return "hi";
    }

    static Role[] RoleList() {
        Role[] roles = new Role[7];
        roles[0] = new Role(1, "Без доступа", "По этой роли доступ в систему закрыт");
        roles[1] = new Role(2, "Комплектовщик", "Комплектовщик имеет доступ только к маршруту комплектации на ТСД");
        roles[2] = new Role(3, "Кладовщик", "Тоже сапмое что и комплектовщик, плюс кладовшик может распечатывать списки товара");
        roles[3] = new Role(4, "Заведующий секции", "");
        roles[4] = new Role(5, "Товаровед", "");
        roles[5] = new Role(6, "Оператор", "Отвечает за наполнение стправочников и распределение заданий");
        roles[6] = new Role(7, "Администратор", "Полный доступ к системе");
        return roles;
    }
}
