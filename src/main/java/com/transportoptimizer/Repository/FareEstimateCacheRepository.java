package com.transportoptimizer.Repository;

import com.transportoptimizer.entity.FareEstimate;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface FareEstimateCacheRepository extends MongoRepository<FareEstimate, String> {

    Optional<FareEstimate> findByOriginAndDestinationAndTotalDistanceKm(
            String origin,
            String destination,
            double totalDistanceKm
    );
}
