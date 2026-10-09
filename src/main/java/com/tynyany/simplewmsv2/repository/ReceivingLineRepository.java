package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.ReceivingLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface ReceivingLineRepository extends CrudRepository<ReceivingLineEntity, Integer> {
    ArrayList<ReceivingLineEntity> findAllByReceivingIDAndDel(Integer receiverId, Boolean del);

    /**
     * Зоны хранения, в которые нужно разместить товар, лежащий в ячейке приёмки
     * и ещё не включённый в маршрут размещения.
     */
    @Query(value = "SELECT DISTINCT l.zoneid FROM receiving_line AS rl" +
            " JOIN location AS l ON rl.locationid = l.locationid" +
            " WHERE COALESCE(rl.del, FALSE) = FALSE" +
            "   AND rl.complete = TRUE" +
            "   AND COALESCE(rl.placement_route, FALSE) = FALSE" +
            "   AND rl.receiving_lineid IN (SELECT b.receiving_id FROM stocks AS s" +
            "        JOIN batches AS b ON s.batch_id = b.batch_id" +
            "        WHERE COALESCE(s.del, FALSE) = FALSE AND s.location_id = :receivingLocationId AND s.quantity > 0)",
            nativeQuery = true)
    List<Integer> findZonesWithUnplacedStock(@Param("receivingLocationId") int receivingLocationId);
}
