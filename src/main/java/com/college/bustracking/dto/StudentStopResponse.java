package com.college.bustracking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentStopResponse {
    private Long id;
    private Long studentId;
    private Long busId;
    private Long stopId;
    private String stopName;
}
