package com.college.bustracking.dto;

import com.college.bustracking.entity.BusStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BusResponse {
    private Long id;
    private String busNumber;
    private Long driverId;
    private String driverName;
    private Double currentLat;
    private Double currentLng;
    private Double currentSpeed;
    private Instant lastUpdated;
    private BusStatus status;
    private Instant createdAt;
}
