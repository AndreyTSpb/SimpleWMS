package com.tynyany.simplewmsv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//product_selection
@Controller
@RequestMapping("/picking_route_list")
@RequiredArgsConstructor
public class PickingRouteController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Система управления складом");
        return "piccking_list";
    }

    public static HashMap<String, String> listHeader(){
        HashMap<String, String> arr = new HashMap<>();
        arr.put("timeCreate", "24.03.2025 17:20:23");
        arr.put("route", "5605675");
        arr.put("routeDate", "25.02.2025");
        arr.put("section", "01");
        arr.put("comment_manager", "ручной контроль.");
        arr.put("numDoc", "О3806735");
        arr.put("status", "Активен");
        arr.put("val", "0.2");
        arr.put("wei", "6,09");
        arr.put("sclad", "ТСК № 23 г. Санкт-Петербург");
        arr.put("order", "ПР004139814");
        arr.put("orderDate", "24.03.2025");
        arr.put("customer", "ИП Синичка Л. Н.");
        arr.put("customer2", "ИП Синичка Л. Н.");
        arr.put("address", "190000,\n Российская Федерация, Ленинградская об., Бакситогорский р-н, \n г. Бакситогорск, Бебеля ул, д 117");

        return arr;
    }

    public static List<ProductListPicking> stringsListPicking(){
        List<ProductListPicking> arr = new ArrayList<>();
        arr.add(new ProductListPicking( 1, "109216", "Ручка шарик. Linc CORONA PLUS синий 0,7 мм оранж. шестигран. корп.", "SC1-01-03-3-1", "шт.",48));
        arr.add(new ProductListPicking( 2, "011053", "Ватман А2 594х420 мм Гознак С-Пб 200 г/м2 100 л.", "SC1-01-04-2-1", "шт.",8));
        arr.add(new ProductListPicking( 3, "028712", "Клей-карандаш ErichKrause EXTRA 15 г для бумаги, картона, текстиля быстросох., смывается водой PVP", "SC1-01-05-1-1", "шт.",52));
        arr.add(new ProductListPicking( 4, "029952", "Ручка шарик. Pilot BPS зелен. 0,7 мм прозр. кругл. корп. грип", "SC1-01-06-2-1", "шт.",12));
        arr.add(new ProductListPicking( 5, "109216", "Кнопки канц. GLOBUS 14 мм 50 шт классические без покрытия металл.", "SC1-01-08-3-1", "шт.",4));
        arr.add(new ProductListPicking( 6, "074519", "Папка д/гуаши А4 15 л. АРТформат офс. 230 г/м2", "SC1-02-03-3-1", "шт.",11));
        arr.add(new ProductListPicking( 7, "217429", "Планшет ErichKrause MEGAPOLIS А4 вертик. синий полипропилен выборочный УФ-лак с крышкой", "SC1-02-04-1-1", "шт.",41));
        arr.add(new ProductListPicking( 8, "219551", "Ручка маслян. Pensan MY-TECH черный 0,7 мм дымчат. кругл. корп. игольчатый наконечник", "SC1-02-05-1-1", "шт.",23));
        arr.add(new ProductListPicking( 9, "238878", "Альбом д/рис. 40 л. А4 скреп. Schoolformat ВЕЛИКОЛЕПНЫЕ ЛОШАДИ мел. карт., ВД-лак, офс.", "SC1-02-07-3-1", "шт.",5));
        arr.add(new ProductListPicking( 10, "249243", "Тетрадь 96 л. А5+ кл. скреп. офс. Schoolformat ЦВЕТЫ МИНИМАЛ мел. карт., спл. УФ-лак", "SC1-03-01-3-1", "шт.",5));
        return arr;
    }

    public static class ProductListPicking{
        public int id;
        public String code;
        public String name;
        public String location;
        public String unit;
        public int qnt;

        ProductListPicking(int id, String code, String name, String location, String unit, int qnt){
            this.id = id;
            this.code = code;
            this.name = name;
            this.location = location;
            this.unit = unit;
            this.qnt = qnt;
        }
    }


}
