package com.tynyany.simplewmsv2.controller;

import com.tynyany.simplewmsv2.dao.*;
import com.tynyany.simplewmsv2.models.AddCookie;
import com.tynyany.simplewmsv2.models.DelCookie;
import com.tynyany.simplewmsv2.repository.ProductRepository;
import com.tynyany.simplewmsv2.entity.*;
import com.tynyany.simplewmsv2.exception.UserNotFoundException;
import com.tynyany.simplewmsv2.service.ABCService;
import com.tynyany.simplewmsv2.service.CategoryService;
import com.tynyany.simplewmsv2.service.ProductService;
import com.tynyany.simplewmsv2.service.SupplierService;
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {
    private final String baseUrl = "products";
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ABCService abcService;
    private final SupplierService supplierService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String filter,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @PageableDefault(sort = { "productName" }, direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ProductEntity> page;
        String filterString = "";

        if (filter != null && !filter.isEmpty()) {
            filterString = "&filter="+filter;
            //page = productService.getAllProductWithPagingAndFilter(pageable, "%"+filter+"%");
            page = productService.getAllProductWithPagingAndFilter(pageable, "%"+filter+"%");
        } else {
            page = productService.getAllProductWithPaging(pageable);
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

        model.addAttribute("title", "Список товаров");
        model.addAttribute("baseUrl", baseUrl);

        model.addAttribute("updateMessage", updateMessage);
        model.addAttribute("totalPage", totalPages);
        model.addAttribute("filter", filterString);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("previewPage", currentPage-1);
        model.addAttribute("nextPage", currentPage+1);

        model.addAttribute("productsList", allProductTableString(page));
        model.addAttribute("abcList", abcService.getAll());
        model.addAttribute("categoriesList", categoryService.getAll());
        model.addAttribute("supplierList", supplierService.getAll());

        return "products";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute ProductFormField productTableString, HttpServletResponse response, HttpServletRequest request) {

        if(!productRepository.existsById(productTableString.getProductID())){
            throw new UserNotFoundException("Product not found: id = " + productTableString.getProductID());
        }

        productService.updateProduct(
                new Product(
                        productTableString.getProductID(),
                        productTableString.getProductName(),
                        productTableString.getProductCode(),
                        productTableString.getDescription(),
                        Float.valueOf(productTableString.getWeight().replace(',', '.')),
                        Float.valueOf(productTableString.getVolume().replace(',', '.')),
                        productTableString.getCategoryID(),
                        productTableString.getAbcID(),
                        productTableString.getExpirationDateRequired(),
                        productTableString.getDel(),
                        productTableString.getSupplierID(),
                        productTableString.getExtBarcode(),
                        productTableString.getIntBarcode()
                )
        );
        String cookieName = "alertMessage";
        //Add Cookie
        response.addCookie(new AddCookie(cookieName, "Обновлен_товар_код:_" + productTableString.getProductCode()).getCookie());

        return "redirect:/" + baseUrl;
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

    public HashMap<Integer, ABC> abcHashMap(){
        List<ABC> abcList = abcService.getAll();

        HashMap<Integer, ABC> arr = new HashMap<>();
        for(ABC abc : abcList){
            arr.put(abc.getAbcID(), abc);
        }
        return arr;
    }

    public static Product[] producstList(){

        Product[] productsList = new Product[10];
        productsList[0] = new Product(0, "Ручка шарик. Linc CORONA PLUS синий 0,7 мм оранж. шестигран. корп.", "109216", "", 0.01F, 0.01F, 3, 1, false, false, 2, "", "20002525245");
        productsList[1] = new Product(1, "Ватман А2 594х420 мм Гознак С-Пб 200 г/м2 100 л.", "011053", "", 0.03F, 0.02F, 2,1, false, false, 1, "", "20002525245");
        productsList[2] = new Product(2, "Клей-карандаш ErichKrause EXTRA 15 г для бумаги, картона, текстиля быстросох., смывается водой PVP", "028712", "", 0.04F, 0.01F, 1,1,  false, false, 2,  "46002525245", "20002525245");
        productsList[3] = new Product(3, "Ручка шарик. Pilot BPS зелен. 0,7 мм прозр. кругл. корп. грип", "029952", "", 0.015F, 0.01F, 3, 1, false, false, 2, "", "20002525245");
        productsList[4] = new Product(4, "Кнопки канц. GLOBUS 14 мм 50 шт классические без покрытия металл.", "109216", "", 0.03F, 0.015F, 1, 2, false, false, 2, "46002525245", "20002525245");
        productsList[5] = new Product(5, "Папка д/гуаши А4 15 л. АРТформат офс. 230 г/м2", "074519", "", 0.02F, 0.009F, 1, 1, false, false, 2,  "46002525245", "20002525245");
        productsList[6] = new Product(6, "Планшет ErichKrause MEGAPOLIS А4 вертик. синий полипропилен выборочный УФ-лак с крышкой", "217429", "", 0.07F, 0.02F, 5,3,  false, false, 2,  "46002525245", "20002525245");
        productsList[7] = new Product(7, "Ручка маслян. Pensan MY-TECH черный 0,7 мм дымчат. кругл. корп. игольчатый наконечник", "219551", "", 0.01F, 0.005F, 3, 4,  false, false, 2,  "", "20002525245");
        productsList[8] = new Product(8, "Альбом д/рис. 40 л. А4 скреп. Schoolformat ВЕЛИКОЛЕПНЫЕ ЛОШАДИ мел. карт., ВД-лак, офс.", "238878", "", 0.04F, 0.02F, 2, 2, false, false, 2,  "46002525245", "20002525245");
        productsList[9] = new Product(9, "Тетрадь 96 л. А5+ кл. скреп. офс. Schoolformat ЦВЕТЫ МИНИМАЛ мел. карт., спл. УФ-лак", "249243", "", 0.02F, 0.01F, 4, 1, false, false, 2,  "", "20002525245");
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

    public  HashMap<Integer, Category> categoriesHashMap(){
        List<Category> categoryList = categoryService.getAll();
        HashMap<Integer, Category> arr = new HashMap<>();
        for(Category category : categoryList){
            arr.put(category.getCategoryID(), category);
        }
        return arr;
    }

    public static UnitOfMeasure[] unitList(){
        UnitOfMeasure[] unitOfMeasures = new UnitOfMeasure[3];
        unitOfMeasures[0] = new UnitOfMeasure(0, "Не указана еденица", "Нет", false);
        unitOfMeasures[1] = new UnitOfMeasure(1, "Штука", "Шт.", false);
        unitOfMeasures[2] = new UnitOfMeasure(2, "Упаковка", "Уп.", false);
        return unitOfMeasures;
    }

    public static UnitOfMeasure[] unitHashMap(){
        UnitOfMeasure[] unitOfMeasures = new UnitOfMeasure[3];
        unitOfMeasures[0] = new UnitOfMeasure(0, "Не указана еденица", "Нет", false);
        unitOfMeasures[1] = new UnitOfMeasure(1, "Штука", "Шт.", false);
        unitOfMeasures[2] = new UnitOfMeasure(2, "Упаковка", "Уп.", false);
        return unitOfMeasures;
    }

    public  HashMap<Integer, Supplier> supplierHashMap(){
        List<Supplier> supplierList = supplierService.getAll();
        HashMap<Integer, Supplier> arr = new HashMap<>();

        for(Supplier supplier : supplierList){
            arr.put(supplier.getSupplierID(), supplier);
        }
        return arr;
    }

    public List<ProductTableString> allProductTableString(Page<ProductEntity> iterable) {

        ArrayList<ProductTableString> products = new ArrayList<>();
        if(iterable.isEmpty()){
            products.add(new ProductTableString(
                    new Product(0, "0", "0", "0", 0F, 0F, 0, 0, false, false, 0, "0", "0"),
                    "0",
                    "0",
                    "0")
            );
            return products;
        }
        for (ProductEntity productEntity : iterable) {
            //Category Name
            String categoryName = "";
            if(productEntity.getCategoryID() != 0){
                Category category = categoryService.getByID(productEntity.getCategoryID());
                if(category != null){
                    categoryName = category.getCategoryName();
                }
            }
            // ABC name
            String adcName = "";
            if(productEntity.getAbcID() != 0){
                ABC abc = abcService.getByID(productEntity.getAbcID());
                if(abc != null){
                    adcName = abc.getCode();
                }
            }

            // Supplier
            String supplierName = "";
            if(productEntity.getSupplierID() != 0){
                Supplier supplier = supplierService.getByID(productEntity.getSupplierID());
                if(supplier != null){
                    supplierName = supplier.getSupplierName();
                }
            }

            products.add(new ProductTableString(
                    new Product(
                            productEntity.getProductID(),
                            productEntity.getProductName(),
                            productEntity.getProductCode(),
                            productEntity.getDescription(),
                            productEntity.getWeight(),
                            productEntity.getVolume(),
                            productEntity.getCategoryID(),
                            productEntity.getAbcID(),
                            productEntity.getExpirationDateRequired(),
                            productEntity.getDel(),
                            productEntity.getSupplierID(),
                            productEntity.getExtBarCode(),
                            productEntity.getIntBarCode()
                    ),
                    categoryName,
                    adcName,
                    supplierName)
            );
        }
        return products;
    }
}
