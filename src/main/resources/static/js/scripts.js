$(document).ready(function () {

    let product_1 = {
        supplierCode: "B29020",
        volume: 0.00343,
        weight: 2.34,
        extBarcode: "4603976100024",
        intBarcode: "2100000645480",
        productCode: "024548",
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
        productCode: "033894",
        productName: "Маркер перманент. Centropen PERMANENT 1 мм черный круглый 1 шт",
        categoryName: "Маркеры перманентные",
        unitOfMeasure: "шт",
        upacovka: "1\/10\/200",
        abcCode: "A"
    };

    let product_list = new Array();

    product_list[0] = product_1;
    product_list[1] = product_2;
    let json = JSON.stringify(product_list);

    let btnUpdateProducts = document.getElementById('addProduct');

    if(btnUpdateProducts != null){
        btnUpdateProducts.addEventListener("click", ()=>{
            $('#addModal').modal('show');
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
                    $('#addModal').modal('hide');
                    $('#upgrad-box').show();
                    $('#upgrad-box-text').text(data.message);
                },
                error: function (e) {

                    console.log("ERROR : ", e);

                }
            });
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