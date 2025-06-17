package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.models.DelCookie;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.entity.*;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.ReceivingService;
import com.tynyany.simplewmsv2.service.RoleService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/receiving")
@RequiredArgsConstructor
public class ReceivingController {

    private final RoleService roleService;
    private final EmployeeService employeeService;
    private final ReceivingService receivingService;

    private final ProductRepository productRepository;
    private final ReceivingLineRepository receivingLineRepository;
    private final ReceivingRepository receivingRepository;


    final String baseUrl = "receiving";

    private int sumQnt = 0;
    private float sumWeight = 0;
    private float sumVolume = 0;


    @GetMapping("/list")
    public String index(@RequestParam(required = false, defaultValue = "") String filter,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @PageableDefault(sort = { "receivingDate" }, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReceivingEntity> page;
        String filterString = "";

        if (filter != null && !filter.isEmpty()) {
            filterString = "&filter="+filter;
            page = receivingService.getAllPageableWithFilter(pageable, "%"+filter+"%");
        } else {
            page = receivingService.getAllPageable(pageable);
        }

        model.addAttribute("title", "Страница со списком всех приходов товаров");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("receivingList", arrayReceivingList(page));
        model.addAttribute("headerList", PickingRouteController.listHeader());
        model.addAttribute("stringList", PickingRouteController.stringsListPicking());

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

        // сообщение об обновлении данных
        model.addAttribute("updateMessage", updateMessage);

        // пагинация
        model.addAttribute("totalPage", totalPages);
        model.addAttribute("filter", filterString);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("previewPage", currentPage-1);
        model.addAttribute("nextPage", currentPage+1);

        return "receiving_list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model){
        model.addAttribute("id", id);
        model.addAttribute("orderHead", orderHead(id));
        model.addAttribute("orderLines", orderLine(id));
        model.addAttribute("orderItog", orderItog());
        return "edit_receiving";
    }

    @GetMapping("/torg_12/{id}")
    public String torg12(){
        return "torg-12";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") int id, Model model){
        model.addAttribute("id", id);
        model.addAttribute("orderHead", orderHead(id));
        model.addAttribute("orderLines", orderLine(id));
        model.addAttribute("orderItog", orderItog());
        return "edit_receiving";
    }

    private HashMap<String, String> orderItog() {
        HashMap<String, String> itog = new HashMap<>();
        itog.put("sunQnt", String.valueOf(this.sumQnt));
        itog.put("sumWeight", String.valueOf(Math.floor(this.sumWeight)));
        itog.put("sumVolume", String.valueOf(Math.floor(this.sumVolume)));
        return itog;
    }

    private  List<HashMap<String, String>> arrayReceivingList(Page<ReceivingEntity> page){

        System.out.println(page);
        List<HashMap<String, String>> arr = new ArrayList<>();

        Status[] statuses = receivingStatuses();

        int kol = 1;
        if (page != null ){
            for(ReceivingEntity item : page){
                System.out.println(item);
                HashMap<String, String> row = new HashMap<>();
                row.put("ID", Integer.toString(item.getReceivingID()));
                row.put("dtWaiting", (item.getReceivingDate() != null ) ? new SimpleDateFormat("yyyy-MM-dd").format(item.getReceivingDate()) : "0000-00-00");
                row.put("dtReal", (item.getGetReceivingDate() != null)? new SimpleDateFormat("yyyy-MM-dd").format(item.getGetReceivingDate()) : "0000-00-00");
                row.put("numOrder", item.getDocumentNumber());
                row.put("employee", Integer.toString(item.getEmployeeID()));

                row.put("sumWeight", "0");
                row.put("sumVolume", "0");
                row.put("qntRow", "0");

                row.put("status", statuses[item.getStatusID()].getName());
                System.out.println(row);

                arr.add(row);
            }
        }else{
            //Нет записей
            HashMap<String, String> row = new HashMap<>();
            row.put("ID", Integer.toString(0));
            row.put("dtWaiting", "ничего нет");
            row.put("dtReal", "ничего нет");
            row.put("numOrder", "ничего нет");
            row.put("employee", "ничего нет");
            row.put("sumWeight", "ничего нет");
            row.put("sumVolume", "ничего нет");
            row.put("qntRow", "ничего нет");
            row.put("status", "ничего нет");

            arr.add(0,row);
        }


        return arr;
    }


    //пример
    private  HashMap<String, String> orderHead(int orderID){
        List<Role> roles = roleService.getAllRole();
        List<Employee> employees = employeeService.getAllEmployee();

        Receiving receiving = receivingList()[orderID]; //Одну выбрали приходную накладную
        Supplier supplier = SuppliersController.supplierList()[receiving.getStatusID()];

        //Новый приход без сотрудника
        Employee employee;
        if(receiving.getEmployeeID() > 0) {
            employee = employees.get(receiving.getEmployeeID());
        }else{
            employee = new Employee(
                    0,
                    "",
                    "",
                    0,
                    0
            );
        }

        HashMap<String, String> orderHead = new HashMap<>();
        orderHead.put("orderId", Integer.toString(orderID));
        orderHead.put("orderERP", receiving.getDocumentNumber());
        orderHead.put("status", "Создан");
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("employeeTabNum", employee.getTabNum());
        orderHead.put("employeeName", employee.getEmployeeName());
        orderHead.put("employeeRole", (employee.getRoleID() > 0) ? roles.get(employee.getRoleID()).getRoleName() : "");
        orderHead.put("orderDate", String.valueOf(receiving.getReceivingDate()));
        orderHead.put("factOrderDate", String.valueOf(receiving.getGetReceivingDate()));
        return orderHead;
    }

    private List<HashMap<String, String>> orderLine(int orderId){
        List<HashMap<String, String>> arr = new ArrayList<>();
        int kol = 1;
        for(ReceivingLine item : receivingLinesList()){
            HashMap<String, String> strArr = new HashMap<>();

            Product product = ProductsController.producstList()[item.getProductID()]; //Данные по продукту

            int qntOrder = item.getQuantityProduct();
            int qntFact = item.getQuantityReceived();
            float weightOrder = product.getWeight()*qntOrder;
            float volumeOrder = product.getVolume()*qntOrder;
            float weightFact = product.getWeight()*qntFact;
            float volumeFact = product.getVolume()*qntFact;


            this.sumQnt += qntFact;
            this.sumWeight += weightFact;
            this.sumVolume += volumeFact;

            strArr.put("kol", String.valueOf(kol++));
            strArr.put("productID", String.valueOf(item.getProductID()));
            strArr.put("name", ProductsController.producstList()[item.getProductID()].getProductName());
            strArr.put("code", ProductsController.producstList()[item.getProductID()].getProductCode());
            strArr.put("ext_barcode", ProductsController.producstList()[item.getProductID()].getExtBarcode());
            strArr.put("int_barcode", ProductsController.producstList()[item.getProductID()].getIntBarcode());
            strArr.put("qntOrder", String.valueOf(qntOrder));
            strArr.put("qntFact", String.valueOf(qntFact));
            strArr.put("weightOrder", String.valueOf(Math.floor(weightOrder)));
            strArr.put("weightFact", String.valueOf(Math.floor(weightFact)));
            strArr.put("volumeOrder", String.valueOf(Math.floor(volumeOrder)));
            strArr.put("volumeFact", String.valueOf(Math.floor(volumeFact)));
            strArr.put("expirationDate", String.valueOf(item.getExpirationDate()));
            strArr.put("note", item.getNote());

            arr.add(strArr);
        }
        return  arr;
    }

    public static Receiving[] receivingList(){
        Receiving[] receivings = new Receiving[2];

        receivings[0] = new Receiving(0, java.sql.Timestamp.valueOf( "2025-02-25 11:10:00" ), java.sql.Timestamp.valueOf( "2025-02-25 12:10:00" ), "З30015266", 2, 0, 1, false);
        receivings[1] = new Receiving(1, java.sql.Timestamp.valueOf( "2025-02-25 11:10:00" ), java.sql.Timestamp.valueOf( "2025-02-25 12:10:00" ), "З30015266", 2, 0, 1, false);

        return receivings;
    }

    public static ReceivingLine[] receivingLinesList(){
        ReceivingLine[] receivingLines = new ReceivingLine[5];
        receivingLines[0] = new ReceivingLine(0, 200, 120, java.sql.Timestamp.valueOf( "2027-02-25 00:00:00" ), 1, 0, 3, false, "Механические повреждения");
        receivingLines[1] = new ReceivingLine(1, 205, 200, java.sql.Timestamp.valueOf( "2027-02-25 00:00:00" ), 1, 0, 2, false, "Просрочены 5 штук");
        receivingLines[2] = new ReceivingLine(2, 100, 100, java.sql.Timestamp.valueOf( "2027-02-25 00:00:00" ), 1, 0, 1, false, "");
        receivingLines[3] = new ReceivingLine(3, 135, 132, java.sql.Timestamp.valueOf( "2027-02-25 00:00:00" ), 1, 0, 4, false, "Повреждена упаковка");
        receivingLines[4] = new ReceivingLine(4, 160, 152, java.sql.Timestamp.valueOf( "2027-02-25 00:00:00" ), 1, 0, 5, false, "Нечитаема этикетка");
        return  receivingLines;
    }

    public static Status[] receivingStatuses(){
        Status[] receivingStatuses = new Status[5];
        receivingStatuses[0] = new Status(0, "В ожидании");
        receivingStatuses[1] = new Status(1, "В процессе");
        receivingStatuses[2] = new Status(2, "Завершена");
        receivingStatuses[3] = new Status(3, "Отменена");
        return receivingStatuses;
    }


}
