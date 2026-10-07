package com.college.bustracking.dto;

import com.college.bustracking.entity.TripStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TripResponse {
    private Long id;
    private Long busId;
    private String busNumber;
    private Long routeId;
    private String routeName;
    private Long driverId;
    private String driverName;
    private Instant startedAt;
    private Instant endedAt;
    private TripStatus status;
    private Instant createdAt;
}
