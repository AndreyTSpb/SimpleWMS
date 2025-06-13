$(document).ready(function () {

    let product_1 = {
        supplierCode: "B29020",
        volume: 0.00343,
        weight: 2.34,
        extBarcode: "4603976100024",
        intBarcode: "2100000645480",
        productCode: "024748",
        productName: "Бумага Снегурочка 500 л. 80 г\/м2 А4 марка С",
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
        productCode: "034894",
        productName: "Маркер перманент. Centropen PERMANENT 1 мм черный круглый 1 шт",
        categoryName: "Маркеры перманентные",
        unitOfMeasure: "шт",
        upacovka: "1\/10\/200",
        abcCode: "B"
    };

    let product_list = new Array();

    product_list[0] = product_1;
    product_list[1] = product_2;
    let json = JSON.stringify(product_list);

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