package com.college.bustracking.controller;

import com.college.bustracking.dto.BusLocationHistoryResponse;
import com.college.bustracking.dto.BusResponse;
import com.college.bustracking.dto.CreateBusRequest;
import com.college.bustracking.dto.CreateUserRequest;
import com.college.bustracking.dto.DashboardResponse;
import com.college.bustracking.dto.RouteResponse;
import com.college.bustracking.dto.StopSuggestionResponse;
import com.college.bustracking.dto.TripResponse;
import com.college.bustracking.dto.UpdateBusRequest;
import com.college.bustracking.dto.UpdateStopCoordinatesRequest;
import com.college.bustracking.dto.StopResponse;
import com.college.bustracking.dto.UserResponse;
import com.college.bustracking.mapper.EntityMapper;
import com.college.bustracking.repository.BusLocationHistoryRepository;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.repository.TripRepository;
import com.college.bustracking.service.AdminService;
import com.college.bustracking.service.AuthService;
import com.college.bustracking.service.StopSuggestionService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;
    private final EntityMapper mapper;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final BusLocationHistoryRepository historyRepository;
    private final StopSuggestionService stopSuggestionService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard statistics")
    @ApiResponse(responseCode = "200", description = "Dashboard loaded")
    public DashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public List<UserResponse> users() {
        return adminService.users().stream().map(mapper::toUser).toList();
    }

    @GetMapping("/drivers")
    @Operation(summary = "List drivers")
    public List<UserResponse> drivers() {
        return adminService.drivers().stream().map(mapper::toUser).toList();
    }

    @PostMapping("/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a driver")
    public UserResponse createDriver(@Valid @RequestBody CreateUserRequest request) {
        request.setRole(com.college.bustracking.entity.Role.DRIVER);
        return authService.createUser(request);
    }

    @GetMapping("/students")
    @Operation(summary = "List students")
    public List<UserResponse> students() {
        return adminService.students().stream().map(mapper::toUser).toList();
    }

    @GetMapping("/buses")
    @Operation(summary = "List buses")
    public List<BusResponse> buses() {
        return busRepository.findAll().stream().map(mapper::toBus).toList();
    }

    @PostMapping("/buses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create bus")
    public BusResponse createBus(@Valid @RequestBody CreateBusRequest request) {
        return mapper.toBus(adminService.createBus(request));
    }

    @PutMapping("/buses/{id}")
    @Operation(summary = "Update bus")
    @Parameter(name = "id", description = "Bus id")
    public BusResponse updateBus(@PathVariable Long id, @Valid @RequestBody UpdateBusRequest request) {
        return mapper.toBus(adminService.updateBus(id, request));
    }

    @DeleteMapping("/buses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete bus")
    @Parameter(name = "id", description = "Bus id")
    public void deleteBus(@PathVariable Long id) {
        adminService.deleteBus(id);
    }

    @GetMapping("/routes")
    @Operation(summary = "List routes")
    public List<RouteResponse> routes() {
        return routeRepository.findAll().stream().map(mapper::toRoute).toList();
    }

    @PutMapping("/routes/{routeId}/stops/{stopId}/location")
    @Operation(summary = "Correct a route stop location")
    public StopResponse updateRouteStopLocation(@PathVariable Long routeId, @PathVariable Long stopId,
                                                @Valid @RequestBody UpdateStopCoordinatesRequest request) {
        return mapper.toStop(adminService.updateRouteStopCoordinates(routeId, stopId, request));
    }

    @GetMapping("/stop-suggestions")
    @Operation(summary = "List pending student stop suggestions")
    public List<StopSuggestionResponse> stopSuggestions() {
        return stopSuggestionService.pending();
    }

    @PostMapping("/stop-suggestions/{id}/approve")
    @Operation(summary = "Approve a student stop suggestion")
    public StopSuggestionResponse approveStopSuggestion(@PathVariable Long id) {
        return stopSuggestionService.approve(id);
    }

    @PostMapping("/stop-suggestions/{id}/reject")
    @Operation(summary = "Reject a student stop suggestion")
    public StopSuggestionResponse rejectStopSuggestion(@PathVariable Long id) {
        return stopSuggestionService.reject(id);
    }

    @GetMapping("/trips")
    @Operation(summary = "List trip history")
    public List<TripResponse> trips() {
        return tripRepository.findAll().stream().map(mapper::toTrip).toList();
    }

    @GetMapping("/buses/{busId}/history")
    @Operation(summary = "Bus location history")
    @Parameter(name = "busId", description = "Bus id")
    public List<BusLocationHistoryResponse> history(@PathVariable Long busId) {
        return historyRepository.findByBusIdOrderByTimestampDesc(busId).stream().map(mapper::toHistory).toList();
    }
}
