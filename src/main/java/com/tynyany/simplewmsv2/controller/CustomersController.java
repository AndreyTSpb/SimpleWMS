package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.CustomerEntity;
import com.tynyany.simplewmsv2.dao.SupplierEntity;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.DelCookie;
import com.tynyany.simplewmsv2.repository.CustomerRepository;
import com.tynyany.simplewmsv2.entity.Customer;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.CustomerService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

//customer_list
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomersController {
    private final CustomerService customerService;
    private final String baseUrl = "customers";
    private final CustomerRepository customerRepository;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String filter,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @PageableDefault(sort = { "customerName" }, direction = Sort.Direction.ASC) Pageable pageable) {

        Page<CustomerEntity> page;
        String filterString = "";

        if (filter != null && !filter.isEmpty()) {
            filterString = "&filter="+filter;
            page = customerService.getCustomersPageWithFilter(pageable, "%"+filter+"%");
        } else {
            page = customerService.getCustomersPageable(pageable);
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

        //List<Customer> customerList = page;
        model.addAttribute("title", "Список клиентов");
        model.addAttribute("customerList", customerList2(page));
        model.addAttribute("baseUrl", baseUrl);

        // сообщение об обновлении данных
        model.addAttribute("updateMessage", updateMessage);

        // пагинация
        model.addAttribute("totalPage", totalPages);
        model.addAttribute("filter", filterString);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("previewPage", currentPage-1);
        model.addAttribute("nextPage", currentPage+1);

        return "customers";
    }

    @GetMapping("/update")
    public String update(@ModelAttribute Customer customer, HttpServletResponse response) {
        if(!customerRepository.existsById(customer.getCustomerID())){
            response.addCookie(new AddCookie("alertMessage", "Не_найден_клиент_с_ID:_" +customer.getCustomerID()).getCookie());
            return "redirect:/" + baseUrl;
        }
        customerService.update(customer);
        response.addCookie(new AddCookie("alertMessage", "Клиент_обновлен_с_ID:_" +customer.getCustomerID()).getCookie());
        return "redirect:/" + baseUrl;
    }

    public static Customer[] customerList(){
        Customer[] customerList = new Customer[5];
        customerList[0] = new Customer(0, "КЛ15062", "М-Книга", "+7(812)1234567", "mbook@mail.ru", "Спб, Новгородская ул., д.10", false, "10:00", "18:00");
        customerList[1] = new Customer(0, "КЛ15002", "Энергия", "+7(812)1234567", "energy@mail.ru", "Спб, Малая Бронная ул., д.22", false, "09:00", "20:00");
        customerList[2] = new Customer(0, "КЛ15122", "Графика М", "+7(812)1234567", "graf-m@mail.ru", "Спб, Путиловская ул., д.1", false, "10:00", "22:00");
        customerList[3] = new Customer(0, "КЛ15210", "Граффит", "+7(812)1234567", "grafit@mail.ru", "Спб, Кировская ул., д.12", false, "10:00", "20:00");
        customerList[4] = new Customer(0, "КЛ15451", "КанцТорг", "+7(812)1234567", "kanctop@mail.ru", "Спб, Зайцева ул., д.20", false, "10:00", "18:00");
        return customerList;
    }

    private List<Customer> customerList2(Page<CustomerEntity> iterable){
        List<Customer> customerList = new ArrayList<>();
        for(CustomerEntity customerEntity : iterable){
            customerList.add(new Customer(
                    customerEntity.getCustomerId(),
                    customerEntity.getCustomerCode(),
                    customerEntity.getCustomerName(),
                    customerEntity.getPhone(),
                    customerEntity.getEmail(),
                    customerEntity.getAddress(),
                    customerEntity.getDel(),
                    customerEntity.getWorkingTimeStr(),
                    customerEntity.getWorkingTimeEnd()
                )
            );
        }
        return customerList;
    }
}
