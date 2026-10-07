package com.college.bustracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBusRequest {
    @NotBlank
    @Size(max = 50)
    private String busNumber;

    private Long driverId;
}
