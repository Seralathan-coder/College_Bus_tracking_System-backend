package com.college.bustracking.dto;

import com.college.bustracking.entity.BusStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TrackingResponse {
    private Long busId;
    private String busNumber;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double distanceToNextStop;
    private Double distanceToSelectedStop;
    private Integer etaMinutes;
    private String nextStop;
    private String currentStop;
    private BusStatus status;
    private Instant timestamp;
    private Instant lastUpdated;
}
