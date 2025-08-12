package com.tynyany.simplewmsv2.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ResultStockDTOSelectBatchProductLocation {
    @Id //первичный ключ
    int id;
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
    private int r_l_id; //receiving_line_id;

}
