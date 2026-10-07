package com.college.bustracking.dto;

import com.college.bustracking.entity.StopSuggestionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class StopSuggestionResponse {
    private Long id;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private Long routeId;
    private String routeName;
    private String busNumber;
    private Long studentId;
    private String studentName;
    private StopSuggestionStatus status;
    private Instant createdAt;
}
