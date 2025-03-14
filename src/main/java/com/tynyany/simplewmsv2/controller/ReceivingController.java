package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.*;
import com.tynyany.simplewmsv2.service.EmployeeService;
import com.tynyany.simplewmsv2.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/receiving")
@RequiredArgsConstructor
public class ReceivingController {

    private final RoleService roleService;
    private final EmployeeService employeeService;

    final String baseUrl = "receiving";

    private int sumQnt = 0;
    private float sumWeight = 0;
    private float sumVolume = 0;


    @GetMapping("/list")
    public String index(Model model) {
        model.addAttribute("title", "Страница со списком всех приходов товаров");
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("receivingList", arrayReceivingList());
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

    private static List<HashMap<String, String>> arrayReceivingList(){
        List<HashMap<String, String>> arr = new ArrayList<>();
        for(Receiving receiving : receivingList()){
            HashMap<String, String> row = new HashMap<>();
            row.put("ID", Integer.toString(receiving.getReceivingID()));
            row.put("dtWaiting", "2020-01-01 12:12");
            row.put("dtReal", "2030-01-01 12:12");
            row.put("numOrder", "Z3454354545");
            row.put("employee", "Vasa Pupkin");
            row.put("sumWeight", "1500");
            row.put("sumVolume", "5");
            row.put("qntRow", "5");
            row.put("status", "Ожидает");
            arr.add(receiving.getReceivingID(), row);
        }
        return arr;
    }

    private  HashMap<String, String> orderHead(int orderID){
        List<Role> roles = roleService.getAllRole();
        List<Employee> employees = employeeService.getAllEmployee();

        Receiving receiving = receivingList()[orderID]; //Одну выбрали приходную накладную
        ReceivingStatus receivingStatus = receivingStatuses()[receiving.getReceivingStatusID()];
        Supplier supplier = SuppliersController.supplierList()[receiving.getReceivingStatusID()];

        Employee employee = employees.get(receiving.getEmployeeID());

        HashMap<String, String> orderHead = new HashMap<>();
        orderHead.put("orderId", Integer.toString(orderID));
        orderHead.put("orderERP", receiving.getDocumentNumber());
        orderHead.put("status", receivingStatus.getReceivingStatus());
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("employeeTabNum", employee.getTabNum());
        orderHead.put("employeeName", employee.getEmployeeName());
        orderHead.put("employeeRole", roles.get(employee.getRoleID()).getName());
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
        Receiving[] receivings = new Receiving[5];

        receivings[0] = new Receiving(0, java.sql.Timestamp.valueOf( "2025-02-25 11:10:00" ), java.sql.Timestamp.valueOf( "2025-02-25 12:10:00" ), "З30015266", 2, 0, false);
        receivings[1] = new Receiving(1, java.sql.Timestamp.valueOf( "2025-02-26 10:00:00" ), java.sql.Timestamp.valueOf( "2025-02-25 12:10:00" ), "З30015265", 3, 9, false);
        receivings[2] = new Receiving(2, java.sql.Timestamp.valueOf( "2025-02-26 15:00:00" ), java.sql.Timestamp.valueOf( "2025-02-25 12:10:00" ), "З30015456", 2, 0, false);
        receivings[3] = new Receiving(3, java.sql.Timestamp.valueOf( "2025-02-27 08:30:00" ), java.sql.Timestamp.valueOf( "2025-02-27 12:10:00" ), "З30016546", 1, 0, false);
        receivings[4] = new Receiving(4, java.sql.Timestamp.valueOf( "2025-02-27 13:00:00" ), java.sql.Timestamp.valueOf( "2025-02-25 12:10:00" ), "З30023456", 1, 0, false);

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

    public static ReceivingStatus[] receivingStatuses(){
        ReceivingStatus[] receivingStatuses = new ReceivingStatus[5];
        receivingStatuses[0] = new ReceivingStatus(0, "В ожидании");
        receivingStatuses[1] = new ReceivingStatus(1, "В процессе");
        receivingStatuses[2] = new ReceivingStatus(2, "Завершена");
        receivingStatuses[3] = new ReceivingStatus(3, "Отменена");
        return receivingStatuses;
    }


}
