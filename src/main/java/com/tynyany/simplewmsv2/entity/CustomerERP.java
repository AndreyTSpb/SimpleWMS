package com.tynyany.simplewmsv2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@JsonAutoDetect
public class CustomerERP {
    String customerCode;
    String customerName;
    String phone;
    String email;
    String address;
    String workingTimeStr;
    String workingTimeEnd;

    public CustomerERP(String customerCode, String customerName, String phone, String email, String address, String workingTimeStr, String workingTimeEnd){

        this.customerCode = customerCode;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.workingTimeStr = workingTimeStr;
        this.workingTimeEnd = workingTimeEnd;
    }
}
