package com.transportoptimizer.Services;

import com.transportoptimizer.entity.FareEstimate;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class CompareSnapshotStore {

    private final Map<String, FareEstimate> store = new ConcurrentHashMap<>();

    public void save(String snapshotId, FareEstimate estimate) {
        store.put(snapshotId, estimate);
    }

    public FareEstimate get(String snapshotId) {
        return store.get(snapshotId);
    }

    public void remove(String snapshotId) {
        store.remove(snapshotId);
    }
}
