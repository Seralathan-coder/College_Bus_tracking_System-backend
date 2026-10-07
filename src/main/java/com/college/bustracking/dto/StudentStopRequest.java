package com.college.bustracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentStopRequest {
    @NotNull
    private Long busId;

    @NotNull
    private Long stopId;
}
