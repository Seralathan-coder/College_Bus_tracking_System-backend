package com.college.bustracking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartTripRequest {
    @NotNull
    @Positive
    private Long busId;

    @NotNull(message = "routeId is required")
    @Positive
    private Long routeId;
}
