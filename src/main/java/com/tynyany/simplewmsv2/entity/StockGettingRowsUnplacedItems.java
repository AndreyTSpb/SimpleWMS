package com.tynyany.simplewmsv2.entity;

import lombok.Value;

@Value
public class StockGettingRowsUnplacedItems {
    private int batch_id;
    private int product_id;
    private int qnt;
    private String product_name;
    private String int_barcode;
    private String ext_barcode;
    private int loc_id;
    private String loc_code;
    private int x;
    private int y;
    private int z;
    private Float sum_weight;
    private Float sum_volume;
    private int r_l_id; //receiving_line_id
}
