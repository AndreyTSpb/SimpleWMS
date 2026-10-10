// CSRF-токен Spring Security для AJAX-запросов (значения берутся из <meta> в header.html)
(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    if (token && header) {
        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
            }
        });
    }
})();

$(document).ready(function () {

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