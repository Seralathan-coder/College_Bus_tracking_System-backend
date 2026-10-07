package com.college.bustracking.controller;

import com.college.bustracking.dto.LocationUpdateRequest;
import com.college.bustracking.dto.TrackingResponse;
import com.college.bustracking.service.CurrentUserService;
import com.college.bustracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Tag(name = "Tracking")
public class BusLocationController {

    private final TrackingService trackingService;
    private final CurrentUserService currentUserService;

    @PostMapping("/{busId}/location")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Send GPS location for a bus")
    @Parameter(name = "busId", description = "Bus id")
    @ApiResponse(responseCode = "200", description = "Location accepted")
    public TrackingResponse updateLocation(@PathVariable Long busId, @Valid @RequestBody LocationUpdateRequest request) {
        return trackingService.updateLocation(currentUserService.requireUser(), busId, request);
    }
}
