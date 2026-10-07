package com.college.bustracking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveRouteContentsRequest {
    @NotBlank
    @Size(max = 120)
    private String routeName;

    @NotNull
    @Size(min = 2)
    private List<@Valid RouteStopItemRequest> stops;
}
