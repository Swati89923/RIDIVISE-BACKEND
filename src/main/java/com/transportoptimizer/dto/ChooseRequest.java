package com.transportoptimizer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChooseRequest {
    private String userId;
    private String snapshotId;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private String chosenProviderId;
}
