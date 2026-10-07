package com.college.bustracking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BusLocationHistoryResponse {
    private Long id;
    private Long busId;
    private Long tripId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double accuracy;
    private Instant timestamp;
}
