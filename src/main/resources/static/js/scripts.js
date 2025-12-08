$(document).ready(function () {

    let product_1 = {
        supplierCode: "ПО0010951",
        volume: 0.00343,
        weight: 2.34,
        extBarcode: "4603976100026",
        intBarcode: "2100000645481",
        productCode: "024749",
        productName: "Бумага 500 л. 80 г\/м2 А4 марка С",
        categoryName: "Бумага класса С",
        unitOfMeasure: "упак",
        upacovka: "5",
        abcCode: "A"
    };

    let product_2 = {
        supplierCode: "ПО0008253",
        volume: 0.000045,
        weight: 0.00992,
        extBarcode: "8595013612309",
        intBarcode: "2100000238941",
        productCode: "034895",
        productName: "Маркер  Centropen PERMANENT 1 мм черный круглый 1 шт",
        categoryName: "Маркеры перманентные",
        unitOfMeasure: "шт",
        upacovka: "1\/10\/200",
        abcCode: "B"
    };

    let product_3 = {
        supplierCode: "ПО0008253",
        volume: 0.000045,
        weight: 0.00992,
        extBarcode: "8595013612309",
        intBarcode: "2100000238941",
        productCode: "034896",
        productName: "Маркер  Centropen PERMANENT 1 мм зеленый круглый 1 шт",
        categoryName: "Маркеры перманентные",
        unitOfMeasure: "шт",
        upacovka: "1\/10\/200",
        abcCode: "B"
    };

    let product_4 = {
        supplierCode: "ПО0008253",
        volume: 0.000045,
        weight: 0.00992,
        extBarcode: "8595013612309",
        intBarcode: "2100000238941",
        productCode: "034897",
        productName: "Маркер  Centropen PERMANENT 1 мм красный круглый 1 шт",
        categoryName: "Маркеры перманентные",
        unitOfMeasure: "шт",
        upacovka: "1\/10\/200",
        abcCode: "B"
    };
    let product_5 = {
        supplierCode: "ПО0008253",
        volume: 0.000045,
        weight: 0.00992,
        extBarcode: "8595013612309",
        intBarcode: "2100000238941",
        productCode: "034898",
        productName: "Маркер  Centropen PERMANENT 1 мм красный круглый 1 шт",
        categoryName: "Маркеры перманентные",
        unitOfMeasure: "шт",
        upacovka: "1\/10\/200",
        abcCode: "B"
    };

    let product_list = [];

    product_list[0] = product_1;
    product_list[1] = product_2;
    product_list[1] = product_3;
    product_list[1] = product_4;
    product_list[1] = product_5;

    let btnUpdateProducts = document.getElementById('addProduct');

    if(btnUpdateProducts != null){
        btnUpdateProducts.addEventListener("click", ()=>{
            const modalWaiting  = $('#addModal');
            modalWaiting.modal('show');
            //setTimeout(ajaxUpdateProduct(product_list, modalWaiting), 1000);
            setTimeout(() => ajaxUpdateProduct(product_list, modalWaiting), 1000);
        });

    }

    function ajaxUpdateProduct(product_list, modalWaiting){
        $.ajax({
            type: "POST",
            contentType : 'application/json; charset=utf-8',
            url: "/api/update_products",
            data: JSON.stringify(product_list),
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (data) {
                console.log("SUCCESS : ", data);
                modalWaiting.modal('hide');
                const modalAlert = $('#upgrad-box');
                modalAlert.css("display", "block");
                //$('#upgrad-box-text').text(data.message);
                const html = '<div class="col-12">'+
                                        '<div class="card mb-4">'+
                                            '<div class="card-body">'+
                                                '<h4 class="fs15 font-weight-light mb-3">Обновление данных <a class="close-btn"><i class="material-icons" style="padding-top: 2px;">close</i></a></h4>'+
                                                '<p class="text-mute">' + data.message + '</p>'+
                                            '</div>'+
                                        '</div>'+
                                    '</div>';
                modalAlert.append(html);
            },
            error: function (e) {console.log("ERROR : ", e);}
        });
    }

    let btnUpdateSupplier = document.getElementById('addSuppliers');
    if(btnUpdateSupplier != null){
        btnUpdateSupplier.addEventListener("click", ()=>{
            const modalWaiting  = $('#addModal');
            modalWaiting.modal('show');
            let supplier_list = '[{' +
                ' "supplierCode": "ПО0007307",' +
                ' "supplierName": "АНО БУМЦ",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0000461",' +
                ' "supplierName": "АНО ИМЦ",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0011604",' +
                ' "supplierName": "АНО УЦДПО Промтехстрой",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "B33002",' +
                ' "supplierName": "АО БИК СНГ",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0007632",' +
                ' "supplierName": "АО Брянконфи",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "000007",' +
                ' "supplierName": "АО Восток-Сервис-Спецкомплект",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0009196",' +
                ' "supplierName": "АО ГАРДЕН",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0013563",' +
                ' "supplierName": "АО ГЛАВСНАБ",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0009921",' +
                ' "supplierName": "АО ДПД РУС",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "000072",' +
                ' "supplierName": "АО ДХЛ Интернешнл",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0005029",' +
                ' "supplierName": "АО Завод ТО ТБО",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "016077",' +
                ' "supplierName": "АО Ивановоглавснаб",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "013р28",' +
                ' "supplierName": "АО Издательский Дом Пронто-Центр",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0011122",' +
                ' "supplierName": "АО Издательство Высшая школа",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0002024",' +
                ' "supplierName": "АО ИЗОЛЮКС",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0002772",' +
                ' "supplierName": "АО Интерфакс",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0007798",' +
                ' "supplierName": "АО Корпорация ГРИНН",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0009726",' +
                ' "supplierName": "АО Красная Звезда",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0006001",' +
                ' "supplierName": "АО Метахим",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0012238",' +
                ' "supplierName": "АО Мир поздравлений",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "AMA325",' +
                ' "supplierName": "АО ОФИС ПРЕМЬЕР",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0010951",' +
                ' "supplierName": "АО Тандер",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0000378",' +
                ' "supplierName": "АО ЧИП и ДИП",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "B17344",' +
                ' "supplierName": "БУМАГА СЕРВИС ООО",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "BY0062",' +
                ' "supplierName": "Бумажный Мир ООО",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "AC5434",' +
                ' "supplierName": "БумКа ООО",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "ПО0000793",' +
                ' "supplierName": "Все для уборки",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '},' +
                '{' +
                ' "supplierCode": "AC5531",' +
                ' "supplierName": "Дик Плюс ООО",' +
                ' "contactPerson": "Вася Пупкин",' +
                ' "phone": 78121234567,' +
                ' "email": "test@test.ru",' +
                ' "del": 0' +
                '}]';
            setTimeout(() => ajaxSupplierProduct(supplier_list, modalWaiting), 1000);
        });
    }

    function ajaxSupplierProduct(supplier_list, modalWaiting){
        $.ajax({
            type: "POST",
            contentType : 'application/json; charset=utf-8',
            url: "/api/add_supplier",
            data: supplier_list,
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (data) {
                console.log("SUCCESS : ", data);
                modalWaiting.modal('hide');
                const modalAlert = $('#upgrad-box');
                modalAlert.css("display", "block");
                //$('#upgrad-box-text').text(data.message);
                const html = '<div class="col-12">'+
                    '<div class="card mb-4">'+
                    '<div class="card-body">'+
                    '<h4 class="fs15 font-weight-light mb-3">Обновление данных <a class="close-btn"><i class="material-icons" style="padding-top: 2px;">close</i></a></h4>'+
                    '<p class="text-mute">' + data.message + '</p>'+
                    '</div>'+
                    '</div>'+
                    '</div>';
                modalAlert.append(html);
            },
            error: function (e) {console.log("ERROR : ", e);}
        });
    }

    let btnUpdateReceiving = document.getElementById('addReceiving');
    console.log(btnUpdateReceiving);
    if(btnUpdateReceiving != null){
        btnUpdateReceiving.addEventListener("click", ()=>{
            console.log('addReceiving');

            const modalWaiting  = $('#addModal');
            modalWaiting.modal('show');
            //setTimeout(ajaxUpdateProduct(product_list, modalWaiting), 1000);
            let json = '{' +
                '    "header": {' +
                '        "orderERP": "З30017516",' +
                '        "supplierCode": "П000019",' +
                '        "orderDate": "2025-09-27 09:00:00.0"' +
                '    },' +
                '    "body": [' +
                '        {' +
                '            "volume": "2.0",' +
                '            "code": "029952",' +
                '            "ext_barcode": "",' +
                '            "qnt": "200",' +
                '            "weight": "3.0",' +
                '            "int_barcode": "20002525245"' +
                '        },' +
                '        {' +
                '            "volume": "2.0",' +
                '            "code": "028712",' +
                '            "ext_barcode": "46002525245",' +
                '            "qnt": "205",' +
                '            "weight": "8.0",' +
                '            "int_barcode": "20002525245"' +
                '        },' +
                '        {' +
                '            "volume": "2.0",' +
                '            "code": "011053",' +
                '            "ext_barcode": "",' +
                '            "qnt": "100",' +
                '            "weight": "3.0",' +
                '            "int_barcode": "20002525245"' +
                '        },' +
                '        {' +
                '            "volume": "2.0",' +
                '            "code": "109216",' +
                '            "ext_barcode": "46002525245",' +
                '            "qnt": "135",' +
                '            "weight": "4.0",' +
                '            "int_barcode": "20002525245"' +
                '        },' +
                '        {' +
                '            "volume": "1.0",' +
                '            "code": "074519",' +
                '            "ext_barcode": "46002525245",' +
                '            "qnt": "160",' +
                '            "weight": "3.0",' +
                '            "int_barcode": "20002525245"' +
                '        }' +
                '    ]' +
                '}';
            setTimeout(() => ajaxAddReceiving(json, modalWaiting), 1000);
        });

    }

    function ajaxAddReceiving(receiving, modalWaiting){
        $.ajax({
            type: "POST",
            contentType : 'application/json; charset=utf-8',
            url: "/api/new_receiving",
            data: receiving,
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (data) {
                console.log("SUCCESS : ", data);
                modalWaiting.modal('hide');
                const modalAlert = $('#upgrad-box');
                modalAlert.css("display", "block");
                //$('#upgrad-box-text').text(data.message);
                const html = '<div class="col-12">'+
                    '<div class="card mb-4">'+
                    '<div class="card-body">'+
                    '<h4 class="fs15 font-weight-light mb-3">Обновление данных <a class="close-btn"><i class="material-icons" style="padding-top: 2px;">close</i></a></h4>'+
                    '<p class="text-mute">' + data.message + '</p>'+
                    '</div>'+
                    '</div>'+
                    '</div>';
                modalAlert.append(html);
            },
            error: function (e) {console.log("ERROR : ", e);}
        });
    }

    //tabl_del_groups
    // if($('#tabl_del_groups').length > 0) {
    //     $('#tabl_del_groups').on('click', '.btnStatisticsEventModal', function() {
    //         const id = $(this).data('id');
    //         const modalBody = $('#statsModalBody');
    //
    //         $.ajax({
    //             url: route + "/ajax_statistic_events",
    //             type: "POST",
    //             data: {
    //                 id_group: id,
    //             },
    //             dataType: "html",
    //             beforeSend: function(){
    //                 let html = '<div class="spinner-border" role="status">\n' +
    //                     '  <span class="sr-only">Загрузка...</span><h2>LOADING...</h2>\n' +
    //                     '</div>';
    //                 modalBody.html(html);
    //             },
    //             success: function(html){
    //                 console.log(html);
    //                 modalBody.html(html);
    //             }
    //         });
    //     });
    // }
});