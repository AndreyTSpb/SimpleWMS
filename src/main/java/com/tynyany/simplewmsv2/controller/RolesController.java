package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolesController {

    private String baseUrl = "roles";

    @GetMapping
    public String index(Model model) {

        Role[] roles = RoleList();


        model.addAttribute("title", "Список ролей");
        model.addAttribute("baseUel", this.baseUrl);
        model.addAttribute("roleList", roles);
        return "roles_list";
    }

    static Role[] RoleList() {
        Role[] roles = new Role[8];
        roles[0] = new Role(0, "Доступ запрещен", "По этой роли доступ в систему закрыт", true);
        roles[1] = new Role(1, "Комплектовщик", "Комплектовщик имеет доступ только к маршруту комплектации на ТСД", false);
        roles[2] = new Role(2, "Кладовщик", "Тоже сапмое что и комплектовщик, плюс кладовшик может распечатывать списки товара", false);
        roles[3] = new Role(3, "Заведующий секции", "", false);
        roles[4] = new Role(4, "Товаровед", "", false);
        roles[5] = new Role(5, "Оператор", "Оператор склада", false);
        roles[6] = new Role(6, "Управляющий", "Управляющий склада имеет все права кроме добавления пользователей в систему и раздачи прав", false);
        roles[7] = new Role(7, "Администратор", "Максимально возможные права", false);
        return roles;
    }
}
