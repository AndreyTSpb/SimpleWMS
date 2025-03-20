package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class ReceivingERP {
    public String orderERP;
    public String supplierCode;
    public String orderDate;
}
