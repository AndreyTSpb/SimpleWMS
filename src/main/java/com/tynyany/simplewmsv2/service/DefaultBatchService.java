package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.BatchEntity;
import com.tynyany.simplewmsv2.entity.Batch;
import com.tynyany.simplewmsv2.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultBatchService implements BatchService {

    private final BatchRepository batchRepository;

    @Override
    public Batch getById(int batch_id) {
        BatchEntity batchEntity = batchRepository.findById(batch_id).orElse(null);
        if (batchEntity == null) {
            return null;
        }
        return new Batch(
                batchEntity.getBatch_id(),
                batchEntity.getProduct_id(),
                batchEntity.getSupplier_id(),
                batchEntity.getDate_received(),
                batchEntity.getQuantity_received(),
                batchEntity.getExpiry_date(),
                batchEntity.getReceiving_id(),
                batchEntity.getDel()
                );
    }

    @Override
    public List<Batch> getAll() {
        return List.of();
    }

    @Override
    public void add(Batch batch) {
        BatchEntity batchEntity = new BatchEntity(
                0,
                batch.getProduct_id(),
                batch.getSupplier_id(),
                batch.getDate_received(),
                batch.getQuantity_received(),
                batch.getExpiry_date(),
                batch.getReceiving_id(),
                batch.getDel()
        );
        batchRepository.save(batchEntity);
    }

    @Override
    public void update(Batch batch) {
        BatchEntity batchEntity = new BatchEntity(
                batch.getBatch_id(),
                batch.getProduct_id(),
                batch.getSupplier_id(),
                batch.getDate_received(),
                batch.getQuantity_received(),
                batch.getExpiry_date(),
                batch.getReceiving_id(),
                batch.getDel()
        );
        batchRepository.save(batchEntity);
    }

    @Override
    public void delete(int batch_id) {
        Batch batch = getById(batch_id);
        BatchEntity batchEntity = new BatchEntity(
                batch.getBatch_id(),
                batch.getProduct_id(),
                batch.getSupplier_id(),
                batch.getDate_received(),
                batch.getQuantity_received(),
                batch.getExpiry_date(),
                batch.getReceiving_id(),
                true
        );
        batchRepository.save(batchEntity);
    }
}
