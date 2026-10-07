package com.college.bustracking.dto;

import com.college.bustracking.entity.RouteStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class RouteResponse {
    private Long id;
    private String routeName;
    private Long driverId;
    private String driverName;
    private Long busId;
    private String busNumber;
    private RouteStatus status;
    private List<StopResponse> stops;
    private Instant createdAt;
    private Instant updatedAt;
}
