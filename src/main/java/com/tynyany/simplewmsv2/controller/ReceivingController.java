package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.ReceivingEntity;
import com.tynyany.simplewmsv2.models.*;
import com.tynyany.simplewmsv2.domain.ReceivingStatus;
import com.tynyany.simplewmsv2.exception.NotFoundException;
import com.tynyany.simplewmsv2.exception.WmsException;
import com.tynyany.simplewmsv2.security.CurrentUser;
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
import lombok.extern.slf4j.Slf4j;
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
import java.util.Locale;
import java.util.Objects;

@Slf4j
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
    private final ReceivingProcessService receivingProcessService;
    private final PlacementService placementService;




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

        model.addAttribute("title", "Список приходных накладных");
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

        model.addAttribute("orderHead", requireOrderHead(id));
        List<HashMap<String, String>> lines = Objects.requireNonNull(orderLine(id));
        model.addAttribute("orderLines", lines);
        model.addAttribute("orderItog", orderItog(lines));

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
        model.addAttribute("orderHead", requireOrderHead(id));
        List<HashMap<String, String>> lines = orderLine(id);
        model.addAttribute("orderLines", lines);
        model.addAttribute("orderItog", orderItog(lines));
        return "edit_receiving";
    }

    @PostMapping("/start_receiving")
    public String startReceiving(@ModelAttribute FormStartReceiving form, HttpServletResponse response) {
        try {
            receivingProcessService.start(form.getOrderID(), form.getEmployeeID(), Timestamp.valueOf(form.getDate_start()));
            alert(response, "Приёмка начата");
        } catch (WmsException e) {
            log.warn("Операция по накладной отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return "redirect:/" + baseUrl + "/edit/" + form.getOrderID();
    }

    @PostMapping("/stop_receiving")
    public String stopReceiving(@ModelAttribute FormCloseOrDeleteReceiving form, HttpServletResponse response) {
        try {
            receivingProcessService.complete(form.getOrderId());
            alert(response, "Приёмка по накладной " + form.getOrderId() + " завершена");
        } catch (WmsException e) {
            log.warn("Операция по накладной отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return "redirect:/" + baseUrl + "/edit/" + form.getOrderId();
    }

    /**
     * Приёмка строки накладной: факт, партия, остаток в зоне приёмки.
     */
    @PostMapping("/update_string_product")
    public String updateStringProduct(@ModelAttribute ReceivingLineForm form, HttpServletResponse response) {
        try {
            receivingProcessService.processLine(form, CurrentUser.employeeId());
            alert(response, "Строка " + form.getNumLine() + " принята");
        } catch (WmsException e) {
            log.warn("Операция по накладной отклонена: {}", e.getMessage());
            alert(response, e.getMessage());
        }
        return "redirect:/" + baseUrl + "/edit/" + form.getOrderId();
    }

    @GetMapping("/update_placement_route")
    public String updatePlacementRoute(HttpServletResponse response) {
        PlacementService.GenerationResult result = placementService.generateRoutes();
        alert(response, "Создано маршрутов: " + result.routesCreated() + ", строк: " + result.linesCreated());
        return "redirect:/product_placement";
    }

    private HashMap<String, String> requireOrderHead(int id) {
        HashMap<String, String> head = orderHead(id);
        if (head == null) {
            throw new NotFoundException("Приходная накладная " + id + " не найдена");
        }
        return head;
    }

    private void alert(HttpServletResponse response, String message) {
        response.addCookie(new AddCookie("alertMessage", message).getCookie());
    }

    private HashMap<String, String> orderItog(List<HashMap<String, String>> lines) {
        int sumQnt = 0;
        float sumWeight = 0;
        float sumVolume = 0;
        if (lines != null) {
            for (HashMap<String, String> line : lines) {
                sumQnt += Integer.parseInt(line.get("qntFact"));
                sumWeight += Float.parseFloat(line.get("weightFact"));
                sumVolume += Float.parseFloat(line.get("volumeFact"));
            }
        }
        HashMap<String, String> itog = new HashMap<>();
        itog.put("sunQnt", String.valueOf(sumQnt));
        itog.put("sumWeight", String.valueOf(Math.floor(sumWeight)));
        itog.put("sumVolume", String.valueOf(Math.floor(sumVolume)));
        return itog;
    }

    private  List<HashMap<String, String>> arrayReceivingList(Page<ReceivingEntity> page){

        List<HashMap<String, String>> arr = new ArrayList<>();


        int kol = 1;
        if (page != null ){
            for(ReceivingEntity item : page){
                HashMap<String, String> row = new HashMap<>();
                row.put("ID", Integer.toString(item.getReceivingID()));
                row.put("dtWaiting", (item.getReceivingDate() != null ) ? new SimpleDateFormat("yyyy-MM-dd").format(item.getReceivingDate()) : "0000-00-00");
                row.put("dtReal", (item.getGetReceivingDate() != null)? new SimpleDateFormat("yyyy-MM-dd").format(item.getGetReceivingDate()) : "0000-00-00");
                row.put("numOrder", item.getDocumentNumber());
                row.put("employee", getEmployName(item.getEmployeeID()));

                CalculationVolumeAndWeightUponReceipt calc = new CalculationVolumeAndWeightUponReceipt(item.getReceivingID(), receivingLineService, productService);
                row.put("sumWeight", String.format(Locale.ROOT, "%.2f", Math.max(calc.getWeight(), 0)));
                row.put("sumVolume", String.format(Locale.ROOT, "%.2f", Math.max(calc.getVolume(), 0)));
                row.put("qntRow", String.valueOf(calc.getQntProducts()));

                row.put("status", ReceivingStatus.fromCode(item.getStatusID()).getTitle());

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

        final int employeeRoleId = employee.getRoleID();
        HashMap<String, String> orderHead = new HashMap<>();
        orderHead.put("orderId", Integer.toString(orderID));
        orderHead.put("orderERP", receiving.getDocumentNumber());
        orderHead.put("status", ReceivingStatus.fromCode(receiving.getStatusID()).getTitle());
        orderHead.put("statusID", String.valueOf(receiving.getStatusID()));
        orderHead.put("supplierId", Integer.toString(supplier.getSupplierID()));
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("employerId", Integer.toString(employee.getEmployeeID()));
        orderHead.put("employeeTabNum", employee.getTabNum());
        orderHead.put("employeeName", employee.getEmployeeName());
        orderHead.put("employeeRole", roles.stream()
                .filter(r -> r.getRoleID() == employeeRoleId)
                .map(Role::getRoleName)
                .findFirst()
                .orElse(""));
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
            strArr.put("expirationDate", item.getExpirationDate() == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(item.getExpirationDate()));
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


    public  String getEmployName(Integer employeeID){
        //Receiving receiving = receivingService.getByID(form.getOrderId());
        if(employeeID < 1){
            return "";
        }
        Employee employee = employeeService.getEmployeeByID(employeeID);
        return employee.getEmployeeName();
    }


}
