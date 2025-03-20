package com.tynyany.simplewmsv2.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="receivings")
public class ReceivingEntity {
    @Id //первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int receivingID;
    Timestamp receivingDate;
    Timestamp getReceivingDate;
    String documentNumber;
    int receivingStatusID; //"В ожидании", "В процессе", "Завершена", "Отменена"
    int employeeID;
    Boolean del;

}
