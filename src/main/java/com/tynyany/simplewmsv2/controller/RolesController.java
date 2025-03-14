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
}
