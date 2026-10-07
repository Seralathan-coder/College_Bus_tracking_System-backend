package com.college.bustracking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {
    private long totalBuses;
    private long activeBuses;
    private long offlineBuses;
    private long drivers;
    private long students;
    private long activeTrips;
}
