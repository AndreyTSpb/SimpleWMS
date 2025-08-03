package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import com.tynyany.simplewmsv2.entity.ReceivingLine;
import com.tynyany.simplewmsv2.entity.ReceivingLineForm;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.service.ReceivingLineService;

import javax.swing.text.html.Option;
import java.sql.Timestamp;

public class UpdateStringReceiving {

    private int receivingStringId;

    private int productId;
    private String extBarcode;
    private String intBarcode;
    private int qntOrder;
    private int qntFact;
    private String expirationDate;
    private String note;
    private int zoneId;
    private int locationId;

    private  ReceivingRepository receivingRepository;
    private ReceivingLineService receivingLineService;

    public UpdateStringReceiving(ReceivingLineForm receivingLineForm){
        System.out.println(receivingLineForm);
        //ReceivingLineForm(numLine=1, productId=33, extBarcode=4600709420174, intBarcode=2004637788393, qntOrder=200, qntFact=10, expirationDate=, note=111111 еуые еуым, zoneId=1, locationId=1)
        this.receivingStringId = receivingLineForm.getNumLine();
        this.productId = receivingLineForm.getProductId();
        this.extBarcode = receivingLineForm.getExtBarcode();
        this.intBarcode = receivingLineForm.getIntBarcode();
        this.qntOrder = receivingLineForm.getQntOrder();
        this.qntFact  = receivingLineForm.getQntFact();
        if(receivingLineForm.getExpirationDate().isEmpty()){
            this.expirationDate = "2025-12-31";
        }else{
            this.expirationDate = receivingLineForm.getExpirationDate();
        }
        this.note = receivingLineForm.getNote();
        this.zoneId = receivingLineForm.getZoneId();
        this.locationId = receivingLineForm.getLocationId();
    }

    public void updateReceivingLine(){
        ReceivingLine receivingLine = receivingLineService.getByID(this.receivingStringId);
        System.out.println(receivingLine);
        if (receivingLine == null){
            System.out.println("NNOT line");

        }
        assert receivingLine != null;
        receivingLineService.update(
                new ReceivingLine(
                    receivingLine.getReceivingLineID(),
                    receivingLine.getQuantityProduct(),
                    this.qntFact,
                    Timestamp.valueOf(this.expirationDate + " 00:00:00.123456789"),
                    receivingLine.getReceivingID(),
                    this.locationId,
                    receivingLine.getProductID(),
                    receivingLine.getDel(),
                    this.note
                )
        );
    }

}
