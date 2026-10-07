package com.college.bustracking.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBusRequest {
    @Size(max = 50)
    private String busNumber;
    private Long driverId;
}
