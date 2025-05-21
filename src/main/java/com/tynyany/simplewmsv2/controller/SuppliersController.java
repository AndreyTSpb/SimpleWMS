package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.SupplierRepository;
import com.tynyany.simplewmsv2.entity.ABC;
import com.tynyany.simplewmsv2.entity.Supplier;
import com.tynyany.simplewmsv2.entity.User;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SuppliersController {

    private  final SupplierService supplierService;
    private final SupplierRepository supplierRepository;
    private final String baseUrl = "suppliers";

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Список поставщиков");
        model.addAttribute("supplierList", supplierHashMap());
        model.addAttribute("baseUrl", baseUrl);
        return "suppliers";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Supplier supplier) {
        supplierService.add(supplier);
        return "redirect:/" + baseUrl;
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Supplier supplier){
        if(!supplierRepository.existsById(supplier.getSupplierID()))
            throw new UserNotFoundException("Supplier not found: id = " + supplier.getSupplierID());
        supplierService.update(supplier);
        return "redirect:/" + baseUrl;
    }

    @RequestMapping("/del")
    public String delUser(@RequestParam(value = "supplierId", required = true) int supplierId){
        if (!supplierRepository.existsById(supplierId))
            throw new UserNotFoundException("User not found: id = " + supplierId);
        Supplier supplier = supplierService.getByID(supplierId);
        supplierService.del(supplier);
        return "redirect:/" + baseUrl;
    }

    /**
     * УДАЛИТЬ
     * @return
     */
    public static Supplier[] supplierList(){
        Supplier[] supplierList = new Supplier[3];
        supplierList[0] = new Supplier(0, "ООО РиК", "П000009", "Остап", "+7(812)1234567", "ric@mail.ru", false);
        supplierList[1] = new Supplier(1, "Святогорский бумажный комбинат", "П000019", "Людмила Леонидовна", "+7(812)1234567", "sv@mail.ru", true);
        supplierList[2] = new Supplier(2, "Монди", "П000021", "Андрей Андреевич", "+7(812)1234567", "mondi@mail.ru", false);
        return supplierList;
    }

    /**
     * Получение поставщиков в виде списка
     * @return
     */
    public  List<HashMap<String, String>> supplierHashMap(){
        List<Supplier> supplierList = supplierService.getAll();
        List<HashMap<String, String>> arr = new ArrayList<>();

        if(supplierList.isEmpty()){
            arr.add(hashMapSupplier(
                    "ничего нет",
                    "ничего нет" ,
                    "ничего нет",
                    "ничего нет",
                    "ничего нет",
                    "ничего нет",
                    "ничего нет"));
        }else {
            int kol = 0;
            for (Supplier supplier : supplierList) {
                arr.add(hashMapSupplier(
                        Integer.toString(supplier.getSupplierID()),
                        supplier.getSupplierName(),
                        supplier.getSupplierCode(),
                        supplier.getContactPerson(),
                        supplier.getPhone(),
                        supplier.getEmail(),
                        Boolean.toString(supplier.getDel())));
            }
        }
        return arr;
    }

    /**
     * Собираем в асоциативный массив
     * @param id
     * @param name
     * @param code
     * @param contactPerson
     * @param phone
     * @param email
     * @param del
     * @return
     */
    private HashMap<String, String> hashMapSupplier(String id, String name, String code, String contactPerson, String phone, String email, String del) {
        HashMap<String, String> row = new HashMap<>();
        row.put("supplierID", id);
        row.put("supplierName", name);
        row.put("supplierCode", code);
        row.put("contactPerson", contactPerson);
        row.put("phone", phone);
        row.put("email", email);
        row.put("del", del);
        return row;
    }
}
