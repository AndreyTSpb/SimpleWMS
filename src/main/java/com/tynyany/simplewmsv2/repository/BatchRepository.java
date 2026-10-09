package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.BatchEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BatchRepository extends CrudRepository<BatchEntity, Integer> {

    /** Действующие партии, созданные при приёмке строки прихода (batches.receiving_id = id строки). */
    @Query("select b from BatchEntity b where b.receiving_id = :lineId and (b.del = false or b.del is null)")
    List<BatchEntity> findActiveByReceivingLine(@Param("lineId") int receivingLineId);
}
