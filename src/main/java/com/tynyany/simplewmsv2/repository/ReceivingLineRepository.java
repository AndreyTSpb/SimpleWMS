package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;

public interface ReceivingLineRepository extends CrudRepository<ReceivingLineEntity, Integer> {
    ArrayList<ReceivingLineEntity> findAllByReceivingIDAndDel(Integer receiverId, Boolean del);

    /**
     * Выборка списка зон куда требуется переместить товар
     * @return
     */
    @Query(value = "SELECT l.zoneid FROM receiving_line AS rl LEFT JOIN  location l on rl.locationid = l.locationid" +
            " WHERE rl.receivingid IN (SELECT b.receiving_id FROM stocks AS s LEFT JOIN batches AS b on s.batch_id = b.batch_id" +
            " WHERE s.del = false AND s.location_id = 1 AND s.quantity > 0) AND rl.del = false AND complete = true" +
            " GROUP BY l.zoneid", nativeQuery = true)
    List<Integer> findALLbyGroupZoneId();
}
