package com.college.bustracking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateRouteRequest {
    @NotBlank
    @Size(max = 120)
    private String routeName;

    @NotNull
    private Long busId;

    @Valid
    private List<CreateStopRequest> stops = new ArrayList<>();
}
