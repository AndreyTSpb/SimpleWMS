package com.tynyany.simplewmsv2.controller;


import com.tynyany.simplewmsv2.entity.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tynyany.simplewmsv2.controller.ReceivingController.*;

@Controller
@RequestMapping(path = "/api", produces="application/json")
@RequiredArgsConstructor
public class RestApiController {

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

    private static HashMap<String, String> orderHead(int orderID){
        Receiving receiving = receivingList()[orderID]; //Одну выбрали приходную накладную
        ReceivingStatus receivingStatus = receivingStatuses()[receiving.getReceivingStatusID()];
        Supplier supplier = SuppliersController.supplierList()[receiving.getReceivingStatusID()];

        Employee employee = EmployeesController.employeesList()[receiving.getEmployeeID()];

        HashMap<String, String> orderHead = new HashMap<>();
        orderHead.put("orderERP", receiving.getDocumentNumber());
        orderHead.put("supplierName", supplier.getSupplierName());
        orderHead.put("supplierCode", supplier.getSupplierCode());
        orderHead.put("orderDate", String.valueOf(receiving.getReceivingDate()));
        return orderHead;
    }
}
