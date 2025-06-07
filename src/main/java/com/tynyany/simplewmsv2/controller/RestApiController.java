package com.tynyany.simplewmsv2.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tynyany.simplewmsv2.dao.*;
import com.tynyany.simplewmsv2.entity.*;
import com.tynyany.simplewmsv2.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Optional;

import static com.tynyany.simplewmsv2.controller.LocationsController.locationsList;
import static com.tynyany.simplewmsv2.controller.ReceivingController.*;
import static com.tynyany.simplewmsv2.controller.RolesController.RoleList;
import static com.tynyany.simplewmsv2.controller.ZonesController.zonesList;

@Controller
@RequestMapping(path = "/api", produces="application/json")
@RequiredArgsConstructor
public class RestApiController {

    private final EmployeeService employeeService;
    private final ProductService productService;
    private final ABCService abcService;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    private final ABCRepository abcRepository;

    private final SupplierRepository supplierRepository;

    private final SupplierService supplierService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final ReceivingRepository receivingRepository;
    private final ReceivingService receivingService;
    private final ReceivingLineRepository receivingLineRepository;
    private final ReceivingLineService receivingLineService;

    private final RoleService roleService;
    private final ZoneService zoneService;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;

    @RequestMapping(value="/new_receiving", method= RequestMethod.POST)
    @ResponseBody
    public ResponseJson addReceiving(@RequestBody String json) throws JsonProcessingException {

        /*
         * {
         *     "header": {
         *         "orderERP": "З30016546",
         *         "supplierCode": "П000019",
         *         "orderDate": "2025-02-27 08:30:00.0"
         *     },
         *     "body": [
         *         {
         *             "volume": "2.0",
         *             "code": "029952",
         *             "ext_barcode": "",
         *             "qnt": "200",
         *             "weight": "3.0",
         *             "int_barcode": "20002525245"
         *         },
         *         {
         *             "volume": "2.0",
         *             "code": "028712",
         *             "ext_barcode": "46002525245",
         *             "qnt": "205",
         *             "weight": "8.0",
         *             "int_barcode": "20002525245"
         *         },
         *         {
         *             "volume": "2.0",
         *             "code": "011053",
         *             "ext_barcode": "",
         *             "qnt": "100",
         *             "weight": "3.0",
         *             "int_barcode": "20002525245"
         *         },
         *         {
         *             "volume": "2.0",
         *             "code": "109216",
         *             "ext_barcode": "46002525245",
         *             "qnt": "135",
         *             "weight": "4.0",
         *             "int_barcode": "20002525245"
         *         },
         *         {
         *             "volume": "1.0",
         *             "code": "074519",
         *             "ext_barcode": "46002525245",
         *             "qnt": "160",
         *             "weight": "3.0",
         *             "int_barcode": "20002525245"
         *         }
         *     ]
         * }
         */

        ObjectMapper objectMapper = new ObjectMapper();
        // Deserialize the JSON string into an array of User objects
        ReceivingJSON receivingJSONS = objectMapper.readValue(json, ReceivingJSON.class);


        System.out.println(receivingJSONS);
        /*
         * "header": {
         *         "orderERP": "З30016546",
         *         "supplierCode": "П000019",
         *         "orderDate": "2025-02-27 08:30:00.0"
         *     }
         */


        receivingService.add(new Receiving(
                0,
                Timestamp.valueOf(receivingJSONS.header.orderDate),
                null,
                receivingJSONS.header.orderERP,
                0,
                0,
                1, false
        ));

        //идентификатор новой записи - НЕПРАВИЛЬНО ТАК НЕЛЬЗЯ
        int receivingID = receivingRepository.findFirstByOrderByReceivingIDDesc().getReceivingID();

        for (ReceivingLineERP line : receivingJSONS.body){
            /*{
            *             "volume": "1.0",
            *             "code": "074519",
            *             "ext_barcode": "46002525245",
            *             "qnt": "160",
            *             "weight": "3.0",
            *             "int_barcode": "20002525245"
            *         }
            * */
            System.out.println(line.code);
            System.out.println(line.ext_barcode);
            System.out.println(line.int_barcode);
            System.out.println(line.qnt);
            System.out.println(line.volume);
            System.out.println(line.weight);
            System.out.println(productRepository.findOneByProductCode(line.code));
            Optional<ProductEntity> productT = productRepository.findOneByProductCode(line.code);
            if(productT.isEmpty()) {
                //TO DO: СДЕЛАТЬ ЗАПРОС НА ПОЛУЧЕНИЯ ДАННЫХ ПО ТОВАРУ
            }
            if(productT.isPresent()){
                int productID = productT.get().getProductID();
                receivingLineService.add(new ReceivingLine(
                    0,
                    Integer.parseInt(line.qnt),
                    0,
                    null,
                    receivingID,
                    0,
                        productID,
                    false,
                    ""
                ));
            }
        }

        return new ResponseJson(1, "Good");
    }


    @RequestMapping(value="/get_json", method= RequestMethod.GET)
    @ResponseBody
    public HashMap<String, Object> getJson(){
        HashMap<String, Object> arr = new HashMap<>();
//        arr.put("header", orderHead(3));
//        arr.put("body", orderLine(3));
        arr.put("header", "header");
        arr.put("body", "body");
        return arr;
    }

//    private List<HashMap<String, String>> orderLine(int orderId){
//        List<HashMap<String, String>> arr = new ArrayList<>();
//        for(ReceivingLine item : receivingLinesList()){
//            HashMap<String, String> strArr = new HashMap<>();
//
//            Product product = ProductsController.producstList()[item.getProductID()]; //Данные по продукту
//
//            int qntOrder = item.getQuantityProduct();
//            int qntFact = item.getQuantityReceived();
//            float weightOrder = product.getWeight()*qntOrder;
//            float volumeOrder = product.getVolume()*qntOrder;
//
//
//
//            strArr.put("name", ProductsController.producstList()[item.getProductID()].getProductName());
//            strArr.put("code", ProductsController.producstList()[item.getProductID()].getProductCode());
//            strArr.put("ext_barcode", ProductsController.producstList()[item.getProductID()].getExtBarcode());
//            strArr.put("int_barcode", ProductsController.producstList()[item.getProductID()].getIntBarcode());
//            strArr.put("qnt", String.valueOf(qntOrder));
//            strArr.put("weight", String.valueOf(Math.floor(weightOrder)));
//            strArr.put("volume", String.valueOf(Math.floor(volumeOrder)));
//
//            arr.add(strArr);
//        }
//        return  arr;
//    }

    private  HashMap<String, String> orderHead(int orderID){
        Receiving receiving = receivingList()[orderID]; //Одну выбрали приходную накладную
        Status receivingStatus = receivingStatuses()[receiving.getStatusID()];
        Supplier supplier = SuppliersController.supplierList()[receiving.getStatusID()];

        Employee employee = employeeService.getAllEmployee().get(receiving.getEmployeeID());

        HashMap<String, String> orderHead = new HashMap<>();
        orderHead.put("orderERP", receiving.getDocumentNumber());
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("orderDate", String.valueOf(receiving.getReceivingDate()));
        return orderHead;
    }

    //@RequestMapping(value="/update_products", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value="/update_products", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    //@RequestMapping(value = "/loadCityByCountry", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseJson addProducts(@RequestBody String json) throws JsonProcessingException {
        int kol = 0;

        System.out.println(json);

        /*
        * {
            "products":[
                 {
                    "supplierCode": "ПО0008253",
                    "volume": 0.000045,
                    "weight": 0.00992,
                    "extBarcode": "8595013612309",
                    "intBarcode": "2100000238941",
                    "productCode": "023894",
                    "productName": "Маркер перманент. Centropen PERMANENT 1 мм черный круглый 1 шт",
                    "categoryName": "Маркеры перманентные",
                    "unitOfMeasure": "шт",
                    "upacovka": "1\/10\/200",
                    "abcCode": "A"
                },
            ]
            }
            */

        ObjectMapper objectMapper = new ObjectMapper();
        for (ProductERP productERP : objectMapper.readValue(json, ProductERP[].class)){

            /*
             * Проверяем существует ли товар с нужным кодом или нет
             */
            Optional<ProductEntity> productT = productRepository.findOneByProductCode(productERP.getProductCode());

            if(productT.isEmpty()) {
                /*
                 * Формирование продукта для добавления в БД
                 */
                Category category = categoryRepository.findByCategoryName(productERP.getCategoryName());
                ABC abc = abcRepository.findByCode(productERP.getAbcCode());

                int supplierId = 0;
                Optional<SupplierEntity> supplier = supplierRepository.findTopBySupplierCode(productERP.getSupplierCode());
                if (supplier.isPresent()) {
                    final SupplierEntity sup = supplier.get();
                    supplierId = sup.getSupplierID();
                }


                Product product = new Product(
                        0,
                        productERP.getProductName(),
                        productERP.getProductCode(),
                        "",
                        productERP.getWeight(),
                        productERP.getVolume(),
                        category.getCategoryID(),
                        abc.getAbcID(),
                        false,
                        false,
                        supplierId,
                        productERP.getExtBarcode(),
                        productERP.getIntBarcode()

                );
                productService.addProduct(product);
                kol++;
            }
        }

        return new ResponseJson(1, "Товары добавлены: " + kol);
    }

    /**
     * Добавим коды классификаторов
     * @param json
     * @return
     */
    @RequestMapping(value="/add_abc", method= RequestMethod.GET)
    @ResponseBody
    public ResponseJson addABC(@RequestBody String json) {

        for(ABC abc : ProductsController.abcList()) {
            abcService.addABC(abc);
        }

        return new ResponseJson(1, "Good1");
    }

    /**
     * Добавляем категории
     * @param json
     * @return
     */
    @RequestMapping(value="/add_category", method= RequestMethod.GET)
    @ResponseBody
    public ResponseJson addCategory(@RequestBody String json) {

        for(Category category : ProductsController.categoriesList()) {
            categoryService.addCategory(category);
        }

        return new ResponseJson(1, "Good1");
    }

    /**
     * Добавляем категории
     * @param json
     * @return
     */
    @RequestMapping(value="/add_supplier", method= RequestMethod.GET)
    @ResponseBody
    public ResponseJson addSupplier(@RequestBody String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Supplier supplier : objectMapper.readValue(json, Supplier[].class)){
            Optional<SupplierEntity> supplierEntity = supplierRepository.findTopBySupplierCode(supplier.getSupplierCode());
            if(supplierEntity.isEmpty())
                supplierService.add(supplier);
        }

        return new ResponseJson(1, "Good1");
    }

    /**
     * Справочник ролей заполняем ( по факту может лучще отдельный список и все)
     * @return
     */
    @RequestMapping(value="/add_roles", method= RequestMethod.GET)
    @ResponseBody
    public ResponseJson addRoles(){

        for(Role role : RoleList()){
            if(role != null){
                System.out.println(role);
                roleService.addRole(role);
            }
        }
        return new ResponseJson(1, "Обновлен справочник ролей");
    }

    /**
     * Добавление данных по клиентам через запрос
     * @param json
     * @return
     */
    @RequestMapping(value = "/add_customers", method = RequestMethod.POST)
    @ResponseBody
    public ResponseJson addCustomer(@RequestBody String json) throws JsonProcessingException {
        System.out.println(json);
        int kol = 0;
        ObjectMapper objectMapper = new ObjectMapper();

        for (CustomerERP customerERP : objectMapper.readValue(json, CustomerERP[].class)) {
            /*
        Проверка по коду клиента стьли такой уже в базе
         */
            Optional<CustomerEntity> customerEntity = customerRepository.findByCustomerCode(customerERP.getCustomerCode());
            if(customerEntity.isEmpty()) {
                customerService.add(new Customer(0,
                        customerERP.getCustomerCode(),
                        customerERP.getCustomerName(),
                        customerERP.getPhone(),
                        customerERP.getEmail(),
                        customerERP.getAddress(),
                        false,
                        customerERP.getWorkingTimeStr(),
                        customerERP.getWorkingTimeEnd()
                ));
                kol++;
            }
        }


        return new ResponseJson(1, "Клиенты добавлены: " + kol);
    }


    @RequestMapping(value = "/add_zones", method = RequestMethod.GET)
    @ResponseBody
    public ResponseJson addZones() {

        for(Zone zone : zonesList()){
            if(zone != null){
                Optional<ZoneEntity> zoneEntity = zoneRepository.findByZoneName(zone.getZoneName());
                if(zoneEntity.isEmpty()) {
                    zoneService.add(zone);
                }
            }
        }
        return new ResponseJson(1, "Обновлен справочник секций");
    }

    @RequestMapping(value = "/add_locations", method = RequestMethod.GET)
    @ResponseBody
    public ResponseJson addLocations() {
        for (Location location : locationsList()){
            if(location != null){
                Optional<LocationEntity> locationEntity = locationRepository.findByLocationCode(location.getLocationCode());
                if(locationEntity.isEmpty()) {
                    locationService.addLocation(location);
                }
            }
        }
        return new ResponseJson(1, "Обновлен справочник мест хранения");
    }
}
