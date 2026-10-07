package com.college.bustracking.controller;

import com.college.bustracking.dto.BusResponse;
import com.college.bustracking.dto.EtaResponse;
import com.college.bustracking.dto.StopResponse;
import com.college.bustracking.dto.RouteResponse;
import com.college.bustracking.dto.StopSuggestionResponse;
import com.college.bustracking.dto.SuggestStopRequest;
import com.college.bustracking.dto.StudentStopRequest;
import com.college.bustracking.dto.StudentStopResponse;
import com.college.bustracking.dto.TrackingResponse;
import com.college.bustracking.mapper.EntityMapper;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.entity.RouteStatus;
import com.college.bustracking.service.BusStopQueryService;
import com.college.bustracking.service.CurrentUserService;
import com.college.bustracking.service.TrackingService;
import com.college.bustracking.service.StopSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Student")
public class StudentController {

    private final BusRepository busRepository;
    private final TrackingService trackingService;
    private final BusStopQueryService busStopQueryService;
    private final CurrentUserService currentUserService;
    private final EntityMapper mapper;
    private final RouteRepository routeRepository;
    private final StopSuggestionService stopSuggestionService;

    @GetMapping("/api/buses")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','DRIVER')")
    @Operation(summary = "List available buses")
    @ApiResponse(responseCode = "200", description = "Buses listed")
    public List<BusResponse> buses() {
        return busRepository.findAll().stream().map(mapper::toBus).toList();
    }

    @GetMapping("/api/buses/{busId}/stops")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','DRIVER')")
    @Operation(summary = "List stops for a bus")
    @Parameter(name = "busId", description = "Bus id")
    public List<StopResponse> stops(@PathVariable Long busId) {
        return busStopQueryService.stopsForBus(busId).stream().map(mapper::toStop).toList();
    }

    @GetMapping("/api/buses/{busId}/routes")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "List active routes for a bus")
    public List<RouteResponse> routes(@PathVariable Long busId) {
        return routeRepository.findByBusId(busId).stream()
                .filter(route -> route.getStatus() == RouteStatus.ACTIVE)
                .map(mapper::toRoute).toList();
    }

    @PostMapping("/api/student/route/{routeId}/stops")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Request a new stop on a route")
    public StopSuggestionResponse requestStop(@PathVariable Long routeId, @Valid @RequestBody SuggestStopRequest request) {
        return stopSuggestionService.suggest(currentUserService.requireUser(), routeId, request);
    }

    @PostMapping("/api/student/select-stop")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Select a stop for live tracking")
    public StudentStopResponse selectStop(@Valid @RequestBody StudentStopRequest request) {
        return trackingService.selectStop(currentUserService.requireUser(), request);
    }

    @GetMapping("/api/student/tracking/{busId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Live tracking snapshot")
    @Parameter(name = "busId", description = "Bus id")
    public TrackingResponse tracking(@PathVariable Long busId) {
        return trackingService.tracking(busId, currentUserService.requireUser());
    }

    @GetMapping("/api/stop/{stopId}/eta")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','DRIVER')")
    @Operation(summary = "ETA to a stop")
    @Parameter(name = "stopId", description = "Stop id")
    public EtaResponse eta(@PathVariable Long stopId) {
        return trackingService.etaForStop(stopId);
    }
}
