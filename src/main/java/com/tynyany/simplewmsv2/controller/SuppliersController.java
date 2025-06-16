package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.ProductEntity;
import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.DelCookie;
import com.tynyany.simplewmsv2.repository.SupplierRepository;
import com.tynyany.simplewmsv2.entity.Supplier;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.SupplierService;
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
    public String index(@RequestParam(required = false, defaultValue = "") String filter,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @PageableDefault(sort = { "supplierName" }, direction = Sort.Direction.ASC) Pageable pageable) {

        Page<SupplierEntity> page;
        String filterString = "";

        if (filter != null && !filter.isEmpty()) {
            filterString = "&filter="+filter;
            //page = productService.getAllProductWithPagingAndFilter(pageable, "%"+filter+"%");
            page = supplierService.getAllSupplierWithPageableAndFiler(pageable, "%"+filter+"%");
        } else {
            page = supplierService.getAllSupplierWithPageable(pageable);
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

        model.addAttribute("title", "Список поставщиков");
        model.addAttribute("supplierList", supplierHashMap(page));
        model.addAttribute("baseUrl", baseUrl);

        // сообщение об обновлении данных
        model.addAttribute("updateMessage", updateMessage);

        // пагинация
        model.addAttribute("totalPage", totalPages);
        model.addAttribute("filter", filterString);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("previewPage", currentPage-1);
        model.addAttribute("nextPage", currentPage+1);

        return "suppliers";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Supplier supplier) {
        supplierService.add(supplier);
        return "redirect:/" + baseUrl;
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Supplier supplier, HttpServletResponse response){
        System.out.println(supplier);
        if(!supplierRepository.existsById(supplier.getSupplierID())){
            response.addCookie(new AddCookie("alertMessage", "Не_найден_поставщик_с_ID:_" +supplier.getSupplierID()).getCookie());
            return "redirect:/" + baseUrl;
        }
        supplierService.update(supplier);

        //Add Cookie
        response.addCookie(new AddCookie("alertMessage", "Обновлен_поставщик_код:" +supplier.getSupplierCode()).getCookie());
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
    public  List<HashMap<String, String>> supplierHashMap(Page<SupplierEntity> iterable){

        List<HashMap<String, String>> arr = new ArrayList<>();

        if(iterable.isEmpty()){
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
            for (SupplierEntity supplier : iterable) {
                arr.add(hashMapSupplier(
                        Integer.toString(supplier.getSupplierID()),
                        supplier.getSupplierName(),
                        supplier.getSupplierCode(),
                        supplier.getContactPerson(),
                        supplier.getPhoneNumber(),
                        supplier.getEmail(),
                        Boolean.toString(supplier.getDel())));
            }
        }
        return arr;
    }

    /**
     * Собираем в ассоциативный массив
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
