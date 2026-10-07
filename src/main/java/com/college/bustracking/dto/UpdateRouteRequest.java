package com.college.bustracking.dto;

import com.college.bustracking.entity.RouteStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRouteRequest {
    @Size(max = 120)
    private String routeName;
    private RouteStatus status;
}
