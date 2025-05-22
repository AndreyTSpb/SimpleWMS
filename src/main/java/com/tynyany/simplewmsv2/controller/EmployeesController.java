package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.EmployeeRepository;
import com.tynyany.simplewmsv2.entity.Employee;
import com.tynyany.simplewmsv2.entity.Role;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
        System.out.println(roles);

        model.addAttribute("title", "Список сотрудников");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("employeesList", employeeTableData(employeeService.getAllEmployee(), roles));
        model.addAttribute("rolesList", roles);
        return "employee";
    }


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addEmployee(@ModelAttribute FormData formData) {
        formData.print();
        employeeService.addEmployee(new Employee(
                formData.employeeID,
                formData.name + " " + formData.lastName,
                formData.tabNum,
                formData.roleID,
                formData.del
        ));
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


    @Setter
    @Getter
    class FormData {
        // гетеры и сетеры
        private String tabNum;
        private String name;
        private String lastName;
        private int del;
        private int employeeID;
        private int roleID;

        public FormData(String tabNum, String name, String lastName, String del, String employeeID, String roleID) {
            this.tabNum = tabNum;
            this.name = name;
            this.lastName = lastName;
            this.del = Integer.parseInt(del);
            this.employeeID = Integer.parseInt(employeeID);
            this.roleID = Integer.parseInt(roleID);
        }

        public void print() {
            System.out.println(tabNum + " " + name + " " + lastName + " " + del + " " + employeeID + " " + roleID);
        }
    }

    /**
     * Для создания объекта с информацией для строки таблицы
     */
    @Getter
    class StringTableData {
        private int employeeID;
        private String eployeeName;
        private String roleName;
        private Boolean del;
        private String firstName;
        private String lastName;
        private String tabNum;
        private int roleID;


        public StringTableData(int employeeID, String eployeeName, String roleName, String tabNum, int del, int roleID) {
            this.employeeID = employeeID;
            this.eployeeName = eployeeName;
            this.roleName = roleName;
            this.del = (del == 1) ? false : true;

            //если делать выборку аяксом это не нужно
            //разбиваем на имя и фамилию через пробел
            String[] split = eployeeName.split(" ");
            this.firstName = split[0];
            this.lastName = split[1];
            this.tabNum = tabNum;
            this.roleID = roleID;
        }
    }


    /**
     *  Список строк для вывода в таблице сотрудников
     * @param employeeList
     * @param roleList
     * @return
     */
    private List<StringTableData>  employeeTableData(List<Employee> employeeList, List<Role> roleList) {
        List<StringTableData> employeeTableData = new ArrayList<>();
        if(employeeList.isEmpty()){
            // если ничего нет в выборке
            employeeTableData.add(new StringTableData(0, "ничего нет", "ничего нет","ничего нет", 0, 0));
        } else {
            // преобразуем сисок ролей в ассоциативный массив
            Map<Integer, Role> roleMap = new HashMap<>();
            for (Role role : roleList) {
                roleMap.put(role.getRoleID(), role);
            }

            //заполняем массив строк
            for (Employee employee : employeeList) {
                if(employee != null) {
                    System.out.println(employee);
                    employeeTableData.add(
                            new StringTableData(
                                    employee.getEmployeeID(),
                                    employee.getEmployeeName(),
                                    roleMap.get(employee.getRoleID()).getRoleName(),
                                    employee.getTabNum(),
                                    employee.getDel(),
                                    employee.getRoleID()
                            )
                        );
                }
            }

        }
        return employeeTableData;
    }
}
