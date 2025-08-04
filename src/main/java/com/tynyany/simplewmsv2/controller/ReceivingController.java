package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.models.*;
import com.tynyany.simplewmsv2.models.StockMovement;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.repository.ReceivingLineRepository;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.entity.*;
import com.tynyany.simplewmsv2.service.*;
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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    private final BatchRepository batchRepository;


    final String baseUrl = "receiving";
    private final ReceivingLineService receivingLineService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final ZoneService zoneService;
    private final LocationService locationService;

    private final BatchService batchService;
    private final StockService stockService;
    private final StockMovementService stockMovementService;

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
        assert updateMessage != null;
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
    public String edit(@PathVariable("id") int id,
                       Model model,
                       HttpServletRequest request,
                       HttpServletResponse response
    ){

        model.addAttribute("id", id);
        model.addAttribute("baseUrl", baseUrl);

        model.addAttribute("orderHead", Objects.requireNonNull(orderHead(id)));
        model.addAttribute("orderLines", Objects.requireNonNull(orderLine(id)));
        model.addAttribute("orderItog", orderItog());

        //Для селекторов
        model.addAttribute("employeeList", employeeService.getAllEmployee());
        model.addAttribute("zoneList", zoneService.getAll());
        model.addAttribute("locationList", locationService.getAllLocation());

        UpdateMessageAlert updateMessageAlert = new UpdateMessageAlert(request,response);
        if(updateMessageAlert.updateMessage != null)
            model.addAttribute("updateMessage", updateMessageAlert.updateMessage);

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

    @PostMapping("/start_receiving")
    public String startReceiving(@ModelAttribute FormStartReceiving form, HttpServletResponse response){

        if(!receivingRepository.existsById(form.getOrderID())){
            response.addCookie(new AddCookie("alertMessage", "Не_найден_приход_с_ИД:_" + form.getOrderID()).getCookie());
            return "redirect:/" + baseUrl + "/list";
        }

        Receiving receiving = receivingService.getByID(form.getOrderID());

        receivingService.update(new Receiving(
                receiving.getReceivingID(),
                receiving.getReceivingDate(),
                Timestamp.valueOf(form.getDate_start()),
                receiving.getDocumentNumber(),
                1,
                form.getEmployeeID(),
                receiving.getSupplierID(),
                false)
        );

        response.addCookie(new AddCookie("alertMessage", "Обновлена_запись_с_ИД:_" + form.getOrderID()).getCookie());
        return "redirect:/" + baseUrl + "/edit/"+form.getOrderID();
    }

    /**
     * Обновления данных по строке при приемке товара
     * @param arr
     * @param response
     * @return
     */
    @PostMapping("/update_string_product")
    public String updateStringProduct(@ModelAttribute ReceivingLineForm arr, HttpServletResponse response){

        UpdateStringReceiving updateStringReceiving = new UpdateStringReceiving(arr, batchService, receivingLineService);
        //Обновляем строки прихода
        updateStringReceiving.updateReceivingLine();
        //Добавление новой партии
        int batchId = updateStringReceiving.addBatheString();
        //Отмечаем движение по складу и изменяем количество на остатках
        new StockMovement(batchId,0,1,arr.getQntFact(),arr.getProductId(),arr.getEmployerId(),1,0,stockMovementService, stockService);

        response.addCookie(new AddCookie("alertMessage", "Обновлена_строка_с_ИД:_" + arr.getNumLine()).getCookie());
        //Возврашаем на страницу приемки
        return "redirect:/" + baseUrl + "/edit/" + arr.getOrderId();
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

                CalculationVolumeAndWeightUponReceipt calc = new CalculationVolumeAndWeightUponReceipt(item.getReceivingID(), receivingLineService, productService);
                row.put("sumWeight", (calc.getWeight()>0) ? String.valueOf(calc.getWeight()) :"0");
                row.put("sumVolume", (calc.getVolume()>0)? String.valueOf(calc.getVolume()) :"0");
                row.put("qntRow", String.valueOf(calc.getQntProducts()));

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

        Receiving receiving = receivingService.getByID(orderID); //Одну выбрали приходную накладную
        System.out.println(receiving);
        if(receiving == null){
            return null;
        }
        Supplier supplier = supplierService.getByID(receiving.getSupplierID());

        //Новый приход без сотрудника
        Employee employee;
        if(receiving.getEmployeeID() > 0) {
            employee = employeeService.getEmployeeByID(receiving.getEmployeeID());
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
        orderHead.put("status", receivingStatuses()[receiving.getStatusID()].getName());
        orderHead.put("statusID", String.valueOf(receiving.getStatusID()));
        orderHead.put("supplierId", Integer.toString(supplier.getSupplierID()));
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("employerId", Integer.toString(employee.getEmployeeID()));
        orderHead.put("employeeTabNum", employee.getTabNum());
        orderHead.put("employeeName", employee.getEmployeeName());
        orderHead.put("employeeRole", (employee.getRoleID() > 0) ? roles.get(employee.getRoleID()).getRoleName() : "");
        orderHead.put("orderDate", String.valueOf(receiving.getReceivingDate()));
        orderHead.put("factOrderDate", String.valueOf(receiving.getGetReceivingDate()));
        return orderHead;
    }

    private List<HashMap<String, String>> orderLine(int orderId){
        List<ReceivingLine> receivingLines = receivingLineService.getAllLinesForReceiving(orderId);

        if (receivingLines == null){
            return null;
        }
        List<HashMap<String, String>> arr = new ArrayList<>();
        int kol = 1;
        for(ReceivingLine item : receivingLines){
            HashMap<String, String> strArr = new HashMap<>();

            Product product = productService.getProductByID(item.getProductID()); //Данные по продукту

            int qntOrder = item.getQuantityProduct();
            int qntFact = item.getQuantityReceived();
            float weightOrder = product.getWeight() * qntOrder;
            float volumeOrder = product.getVolume() * qntOrder;

            float weightFact  = product.getWeight() * qntFact;
            float volumeFact  = product.getVolume() * qntFact;


            this.sumQnt += qntFact;
            this.sumWeight += weightFact;
            this.sumVolume += volumeFact;

            strArr.put("kol", String.valueOf(kol++));
            strArr.put("rowId", String.valueOf(item.getReceivingLineID()));
            strArr.put("productID", String.valueOf(item.getProductID()));
            strArr.put("name", product.getProductName());
            strArr.put("code", product.getProductCode());
            strArr.put("ext_barcode", product.getExtBarcode());
            strArr.put("int_barcode", product.getIntBarcode());
            strArr.put("qntOrder", String.valueOf(qntOrder));
            strArr.put("qntFact", String.valueOf(qntFact));
            strArr.put("weightOrder", String.valueOf(weightOrder));
            strArr.put("weightFact", String.valueOf(weightFact));
            strArr.put("volumeOrder", String.valueOf(volumeOrder));
            strArr.put("volumeFact", String.valueOf(volumeFact));
            strArr.put("expirationDate", String.valueOf(item.getExpirationDate()));
            strArr.put("note", item.getNote());

            //Предпологаемое место хранения
            int locationId = item.getLocationID();

            int zoneId = 0;
            if(locationId > 0) {
                Location location = locationService.getLocationByID(locationId);
                if(location !=null)
                    zoneId = location.getZoneID();
            }
            strArr.put("zoneId", String.valueOf(zoneId));
            strArr.put("locationId",String.valueOf(locationId));

            //Обработана или нет
            strArr.put("complete", String.valueOf(item.getComplite()));

            arr.add(strArr);
        }
        return  arr;
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
