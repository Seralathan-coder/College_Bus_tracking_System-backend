package com.college.bustracking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class EtaResponse {
    private Long stopId;
    private String stopName;
    private Double distanceKm;
    private Double speedKmh;
    private Integer etaMinutes;
    private String nextStop;
    private Instant lastUpdated;
}
