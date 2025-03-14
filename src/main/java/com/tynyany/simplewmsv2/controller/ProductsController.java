package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {
    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Список товаров");
        model.addAttribute("productsList", producstList());
        model.addAttribute("abcList", abcList());
        model.addAttribute("categoriesList", categoriesList());
        model.addAttribute("unitList", unitList());
        model.addAttribute("supplierList", SuppliersController.supplierList());
        return "products";
    }

    @GetMapping("/add")
    public String addProduct(Model model) {
        model.addAttribute("title", "Добавить товар");
        return "add_user";
    }

    public static ABC[] abcList(){
        ABC[] abcList = new ABC[5];
        //https://rostov-logist.ru/o-logistike-obuchenii-i-konsaltinge/klassifikatsiya-skladov-a-b-c-d/
        abcList[0] = new ABC(0, "0", "Код не выставлен", true);
        abcList[1] = new ABC(1, "A", "Товары из данной категории имеют высокую стоимость либо большой потенциал продаж", false);
        abcList[2] = new ABC(2, "B", "Товары в этой категории имеют среднюю стоимость и потенциал продаж.", false);
        abcList[3] = new ABC(3, "C", "Эта категория включает товары с низкой стоимостью и минимальным объемом продаж.", false);
        abcList[4] = new ABC(4, "D", "Товары из данной категории имеют очень низкую стоимость или полностью перестали продаваться, а иногда даже вышли из строя либо устарели.", false);
        return abcList;
    }

    public static Product[] producstList(){
        Product[] productsList = new Product[10];
        productsList[0] = new Product(0, "Ручка шарик. Linc CORONA PLUS синий 0,7 мм оранж. шестигран. корп.", "109216", "", 0.01F, 0.01F, 3, 1,1, false, false, 2, 1, "", "20002525245");
        productsList[1] = new Product(1, "Ватман А2 594х420 мм Гознак С-Пб 200 г/м2 100 л.", "011053", "", 0.03F, 0.02F, 2, 2,1, false, false, 2, 1, "", "20002525245");
        productsList[2] = new Product(2, "Клей-карандаш ErichKrause EXTRA 15 г для бумаги, картона, текстиля быстросох., смывается водой PVP", "028712", "", 0.04F, 0.01F, 1,1, 1, false, false, 2, 1, "46002525245", "20002525245");
        productsList[3] = new Product(3, "Ручка шарик. Pilot BPS зелен. 0,7 мм прозр. кругл. корп. грип", "029952", "", 0.015F, 0.01F, 3, 1,1, false, false, 2, 1, "", "20002525245");
        productsList[4] = new Product(4, "Кнопки канц. GLOBUS 14 мм 50 шт классические без покрытия металл.", "109216", "", 0.03F, 0.015F, 1, 2,1, false, false, 2, 1, "46002525245", "20002525245");
        productsList[5] = new Product(5, "Папка д/гуаши А4 15 л. АРТформат офс. 230 г/м2", "074519", "", 0.02F, 0.009F, 1, 1,1, false, false, 2, 1, "46002525245", "20002525245");
        productsList[6] = new Product(6, "Планшет ErichKrause MEGAPOLIS А4 вертик. синий полипропилен выборочный УФ-лак с крышкой", "217429", "", 0.07F, 0.02F, 5,3, 1, false, false, 2, 1, "46002525245", "20002525245");
        productsList[7] = new Product(7, "Ручка маслян. Pensan MY-TECH черный 0,7 мм дымчат. кругл. корп. игольчатый наконечник", "219551", "", 0.01F, 0.005F, 3, 4, 1, false, false, 2, 1, "", "20002525245");
        productsList[8] = new Product(8, "Альбом д/рис. 40 л. А4 скреп. Schoolformat ВЕЛИКОЛЕПНЫЕ ЛОШАДИ мел. карт., ВД-лак, офс.", "238878", "", 0.04F, 0.02F, 2, 2,1, false, false, 2, 1, "46002525245", "20002525245");
        productsList[9] = new Product(9, "Тетрадь 96 л. А5+ кл. скреп. офс. Schoolformat ЦВЕТЫ МИНИМАЛ мел. карт., спл. УФ-лак", "249243", "", 0.02F, 0.01F, 4, 1,1, false, false, 2, 1, "", "20002525245");
        return  productsList;
    }

    public static Category[] categoriesList(){
        Category[] categoriesList = new Category[8];
        categoriesList[0] = new Category(0, "Нет категории", "", true);
        categoriesList[1] = new Category(1, "Карандаши чернографитные без ластика", "", false);
        categoriesList[2] = new Category(2, "Ватман", "", false);
        categoriesList[3] = new Category(3, "Ручки шариковые", "", false);
        categoriesList[4] = new Category(4, "Тетради формата А4", "", false);
        categoriesList[5] = new Category(5, "Планшеты", "", false);
        categoriesList[6] = new Category(6, "Маркеры перманентные", "", false);
        categoriesList[7] = new Category(7, "Бумага класса С", "", false);

        return  categoriesList;
    }

    public static UnitOfMeasure[] unitList(){
        UnitOfMeasure[] unitOfMeasures = new UnitOfMeasure[3];
        unitOfMeasures[0] = new UnitOfMeasure(0, "Не указана еденица", "Нет", false);
        unitOfMeasures[1] = new UnitOfMeasure(1, "Штука", "Шт.", false);
        unitOfMeasures[2] = new UnitOfMeasure(2, "Упаковка", "Уп.", false);
        return unitOfMeasures;
    }
}
