package com.tynyany.simplewmsv2.models;

import java.util.Arrays;
import java.util.List;

/**
 * Перечисления для типов перемещения
 */
public class InventoryMovementType {
   private final List<String> invMovType = Arrays.asList("Ожидаем", "Приемка", "Размещение", "Сборка", "Отгрузка", "Корректирование", "CycleCount");

   public String getType(int id){
       return this.invMovType.get(id);
   }
}
