package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Value;

import java.util.ArrayList;

@JsonAutoDetect
public class ReceivingJSON {

    public ReceivingERP header;
    public ArrayList<ReceivingLineERP> body;

    public ReceivingJSON(){

    }

    public ReceivingJSON (ReceivingERP header, ArrayList<ReceivingLineERP> body){
        this.header = header;
        this.body = body;
    }

}
