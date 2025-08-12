package com.tynyany.simplewmsv2.models;

import com.tynyany.simplewmsv2.entity.Batch;
import com.tynyany.simplewmsv2.entity.ReceivingLine;
import com.tynyany.simplewmsv2.entity.ReceivingLineForm;
import com.tynyany.simplewmsv2.repository.ReceivingRepository;
import com.tynyany.simplewmsv2.service.BatchService;
import com.tynyany.simplewmsv2.service.ReceivingLineService;

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
    private int supplierId;
    private int receivingId;

    private  ReceivingRepository receivingRepository;
    private final BatchService batchService;
    private final ReceivingLineService receivingLineService;

    public UpdateStringReceiving(ReceivingLineForm receivingLineForm, BatchService batchService, ReceivingLineService receivingLineService){
        this.batchService = batchService;
        this.receivingLineService = receivingLineService;
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
        this.supplierId = receivingLineForm.getSupplierId();
        this.receivingId = receivingLineForm.getOrderId();
    }

    /**
     * Отмечаем данные в строке приемки
     */
    public void updateReceivingLine(){
        ReceivingLine receivingLine = receivingLineService.getByID(this.receivingStringId);
        if (receivingLine == null){
            System.out.println("NNOT line");

        }
        assert receivingLine != null;
        if(this.supplierId < 1)
            this.receivingId = receivingLine.getReceivingID();

        receivingLineService.update(
                new ReceivingLine(
                        this.receivingStringId,
                        receivingLine.getQuantityProduct(),
                        this.qntFact,
                        Timestamp.valueOf(this.expirationDate + " 00:00:00.123456789"),
                        receivingLine.getReceivingID(),
                        this.locationId,
                        receivingLine.getProductID(),
                        true,
                        receivingLine.getPlacementRoute(),
                        receivingLine.getDel(),
                        this.note
                )
        );
    }

    /**
     * Создается запись в таблице Batches с информацией о партии (поставщик, дата поступления, количество)
     */
    public int addBatheString(){
        Batch batch = new Batch(
                0,
                this.productId,
                this.supplierId,
                new Timestamp(System.currentTimeMillis()),
                this.qntFact,
                Timestamp.valueOf(this.expirationDate + " 00:00:00.123456789"),
                this.receivingStringId,
                false
        );
        return batchService.add(batch);
    }
}
