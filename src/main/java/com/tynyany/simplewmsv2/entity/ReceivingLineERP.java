package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class ReceivingLineERP {
    public String volume;
    public String code;
    public String ext_barcode;
    public String qnt;
    public String weight;
    public String int_barcode;
}
