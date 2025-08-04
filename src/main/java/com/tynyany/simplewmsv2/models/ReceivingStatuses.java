package com.tynyany.simplewmsv2.models;

import java.util.Arrays;
import java.util.List;

public class ReceivingStatuses {
    private final List<String> status = Arrays.asList("В ожидании", "В процессе", "Завершена", "Отменена");

    public String getStatus(int id){
        return this.status.get(id);
    }
}
