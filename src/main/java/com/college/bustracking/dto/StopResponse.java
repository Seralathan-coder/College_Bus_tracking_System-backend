package com.college.bustracking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StopResponse {
    private Long id;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private Integer sequenceOrder;
    private Long routeId;
}
