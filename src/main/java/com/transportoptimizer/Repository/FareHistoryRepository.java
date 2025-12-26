package com.transportoptimizer.Repository;

import com.transportoptimizer.entity.FareHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface FareHistoryRepository extends MongoRepository<FareHistory, String> {

    List<FareHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}
