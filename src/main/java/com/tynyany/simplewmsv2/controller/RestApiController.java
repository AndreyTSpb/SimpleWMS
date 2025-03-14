package com.tynyany.simplewmsv2.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tynyany.simplewmsv2.dao.*;
import com.tynyany.simplewmsv2.entity.*;
import com.tynyany.simplewmsv2.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.tynyany.simplewmsv2.controller.ReceivingController.*;

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

    @RequestMapping(value="/new_receiving", method= RequestMethod.GET)
    @ResponseBody
    public ResponseJson addUser(@RequestBody String json) {

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

        System.out.println(json);
        return new ResponseJson(1, "Good");
    }

    @RequestMapping(value="/get_json", method= RequestMethod.GET)
    @ResponseBody
    public HashMap<String, Object> getJson(){
        HashMap<String, Object> arr = new HashMap<>();
        arr.put("header", orderHead(3));
        arr.put("body", orderLine(3));
        return arr;
    }

    private List<HashMap<String, String>> orderLine(int orderId){
        List<HashMap<String, String>> arr = new ArrayList<>();
        for(ReceivingLine item : receivingLinesList()){
            HashMap<String, String> strArr = new HashMap<>();

            Product product = ProductsController.producstList()[item.getProductID()]; //Данные по продукту

            int qntOrder = item.getQuantityProduct();
            int qntFact = item.getQuantityReceived();
            float weightOrder = product.getWeight()*qntOrder;
            float volumeOrder = product.getVolume()*qntOrder;



            strArr.put("name", ProductsController.producstList()[item.getProductID()].getProductName());
            strArr.put("code", ProductsController.producstList()[item.getProductID()].getProductCode());
            strArr.put("ext_barcode", ProductsController.producstList()[item.getProductID()].getExtBarcode());
            strArr.put("int_barcode", ProductsController.producstList()[item.getProductID()].getIntBarcode());
            strArr.put("qnt", String.valueOf(qntOrder));
            strArr.put("weight", String.valueOf(Math.floor(weightOrder)));
            strArr.put("volume", String.valueOf(Math.floor(volumeOrder)));

            arr.add(strArr);
        }
        return  arr;
    }

    private  HashMap<String, String> orderHead(int orderID){
        Receiving receiving = receivingList()[orderID]; //Одну выбрали приходную накладную
        ReceivingStatus receivingStatus = receivingStatuses()[receiving.getReceivingStatusID()];
        Supplier supplier = SuppliersController.supplierList()[receiving.getReceivingStatusID()];

        Employee employee = employeeService.getAllEmployee().get(receiving.getEmployeeID());

        HashMap<String, String> orderHead = new HashMap<>();
        orderHead.put("orderERP", receiving.getDocumentNumber());
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("orderDate", String.valueOf(receiving.getReceivingDate()));
        return orderHead;
    }

    @RequestMapping(value="/update_products", method= RequestMethod.GET)
    @ResponseBody
    public ResponseJson addProducts(@RequestBody String json) throws JsonProcessingException {
        int kol = 0;

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
             }
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
                        0,
                        false,
                        false,
                        supplierId,
                        0,
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
        // Deserialize the JSON string into an array of User objects
        Supplier[] suppliers = objectMapper.readValue(json, Supplier[].class);

        for (Supplier supplier : suppliers){
            supplierService.add(supplier);
        }

        return new ResponseJson(1, "Good1");
    }

}
