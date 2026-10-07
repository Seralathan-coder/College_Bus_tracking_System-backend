package com.college.bustracking.controller;

import com.college.bustracking.dto.BusResponse;
import com.college.bustracking.dto.CurrentTripResponse;
import com.college.bustracking.dto.CreateRouteRequest;
import com.college.bustracking.dto.CreateStopRequest;
import com.college.bustracking.dto.RouteResponse;
import com.college.bustracking.dto.SaveRouteContentsRequest;
import com.college.bustracking.dto.StartTripRequest;
import com.college.bustracking.dto.StopResponse;
import com.college.bustracking.dto.TripResponse;
import com.college.bustracking.dto.UpdateRouteRequest;
import com.college.bustracking.dto.UpdateStopRequest;
import com.college.bustracking.entity.User;
import com.college.bustracking.mapper.EntityMapper;
import com.college.bustracking.service.CurrentUserService;
import com.college.bustracking.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "Driver")
public class DriverController {

    private final DriverService driverService;
    private final CurrentUserService currentUserService;
    private final EntityMapper mapper;

    @GetMapping("/bus")
    @Operation(summary = "Get assigned bus")
    public BusResponse assignedBus() {
        return mapper.toBus(driverService.requireAssignedBus(currentUserService.requireUser()));
    }

    @GetMapping("/routes")
    @Operation(summary = "List own routes")
    public List<RouteResponse> routes() {
        return driverService.myRoutes(currentUserService.requireUser()).stream().map(mapper::toRoute).toList();
    }

    @PostMapping("/route")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create route with ordered stops")
    @ApiResponse(responseCode = "201", description = "Route created")
    public RouteResponse createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return mapper.toRoute(driverService.createRoute(currentUserService.requireUser(), request));
    }

    @PutMapping("/route/{routeId}")
    @Operation(summary = "Update own route")
    @Parameter(name = "routeId", description = "Route id")
    public RouteResponse updateRoute(@PathVariable Long routeId, @Valid @RequestBody UpdateRouteRequest request) {
        return mapper.toRoute(driverService.updateRoute(currentUserService.requireUser(), routeId, request));
    }

    @PutMapping("/route/{routeId}/contents")
    @Operation(summary = "Save route name and ordered stops")
    public RouteResponse saveRouteContents(@PathVariable Long routeId, @Valid @RequestBody SaveRouteContentsRequest request) {
        return mapper.toRoute(driverService.saveRouteContents(currentUserService.requireUser(), routeId, request));
    }

    @DeleteMapping("/route/{routeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete or archive own route")
    public void deleteRoute(@PathVariable Long routeId) {
        driverService.deleteRoute(currentUserService.requireUser(), routeId);
    }

    @PostMapping("/stop")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add stop to own route")
    public StopResponse addStop(@Valid @RequestBody CreateStopRequest request) {
        return mapper.toStop(driverService.addStop(currentUserService.requireUser(), request));
    }

    @PutMapping("/stop/{stopId}")
    @Operation(summary = "Update stop")
    @Parameter(name = "stopId", description = "Stop id")
    public StopResponse updateStop(@PathVariable Long stopId, @Valid @RequestBody UpdateStopRequest request) {
        return mapper.toStop(driverService.updateStop(currentUserService.requireUser(), stopId, request));
    }

    @DeleteMapping("/stop/{stopId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete stop")
    @Parameter(name = "stopId", description = "Stop id")
    public void deleteStop(@PathVariable Long stopId) {
        driverService.deleteStop(currentUserService.requireUser(), stopId);
    }

    @PostMapping("/trip/start")
    @Operation(summary = "Start trip")
    public TripResponse startTrip(@Valid @RequestBody StartTripRequest request) {
        return mapper.toTrip(driverService.startTrip(currentUserService.requireUser(), request));
    }

    @PostMapping("/trip/{tripId}/pause")
    @Operation(summary = "Pause trip")
    @Parameter(name = "tripId", description = "Trip id")
    public TripResponse pauseTrip(@PathVariable Long tripId) {
        return mapper.toTrip(driverService.pauseTrip(currentUserService.requireUser(), tripId));
    }

    @PostMapping("/trip/{tripId}/resume")
    @Operation(summary = "Resume a paused trip")
    @Parameter(name = "tripId", description = "Trip id")
    public TripResponse resumeTrip(@PathVariable Long tripId) {
        return mapper.toTrip(driverService.resumeTrip(currentUserService.requireUser(), tripId));
    }

    @PostMapping("/trip/{tripId}/end")
    @Operation(summary = "End trip")
    @Parameter(name = "tripId", description = "Trip id")
    public TripResponse endTrip(@PathVariable Long tripId) {
        return mapper.toTrip(driverService.endTrip(currentUserService.requireUser(), tripId));
    }

    @GetMapping("/trip/current")
    @Operation(summary = "Current trip status")
    public CurrentTripResponse currentTrip() {
        return new CurrentTripResponse(driverService.currentTrip(currentUserService.requireUser())
                .map(mapper::toTrip).orElse(null));
    }
}
