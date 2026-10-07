package com.college.bustracking.mapper;

import com.college.bustracking.dto.BusLocationHistoryResponse;
import com.college.bustracking.dto.BusResponse;
import com.college.bustracking.dto.RouteResponse;
import com.college.bustracking.dto.StopResponse;
import com.college.bustracking.dto.TripResponse;
import com.college.bustracking.dto.UserResponse;
import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusLocationHistory;
import com.college.bustracking.entity.Route;
import com.college.bustracking.entity.Stop;
import com.college.bustracking.entity.Trip;
import com.college.bustracking.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityMapper {

    public UserResponse toUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public BusResponse toBus(Bus bus) {
        return BusResponse.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .driverId(bus.getDriver() != null ? bus.getDriver().getId() : null)
                .driverName(bus.getDriver() != null ? bus.getDriver().getName() : null)
                .currentLat(bus.getCurrentLat())
                .currentLng(bus.getCurrentLng())
                .currentSpeed(bus.getCurrentSpeed())
                .lastUpdated(bus.getLastUpdated())
                .status(bus.getStatus())
                .createdAt(bus.getCreatedAt())
                .build();
    }

    public StopResponse toStop(Stop stop) {
        return StopResponse.builder()
                .id(stop.getId())
                .stopName(stop.getStopName())
                .latitude(stop.getLatitude())
                .longitude(stop.getLongitude())
                .sequenceOrder(stop.getSequenceOrder())
                .routeId(stop.getRoute() != null ? stop.getRoute().getId() : null)
                .build();
    }

    public RouteResponse toRoute(Route route) {
        List<StopResponse> stops = route.getStops() == null
                ? List.of()
                : route.getStops().stream().map(this::toStop).toList();
        return RouteResponse.builder()
                .id(route.getId())
                .routeName(route.getRouteName())
                .driverId(route.getDriver().getId())
                .driverName(route.getDriver().getName())
                .busId(route.getBus().getId())
                .busNumber(route.getBus().getBusNumber())
                .status(route.getStatus())
                .stops(stops)
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }

    public TripResponse toTrip(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .busId(trip.getBus().getId())
                .busNumber(trip.getBus().getBusNumber())
                .routeId(trip.getRoute().getId())
                .routeName(trip.getRoute().getRouteName())
                .driverId(trip.getDriver().getId())
                .driverName(trip.getDriver().getName())
                .startedAt(trip.getStartedAt())
                .endedAt(trip.getEndedAt())
                .status(trip.getStatus())
                .createdAt(trip.getCreatedAt())
                .build();
    }

    public BusLocationHistoryResponse toHistory(BusLocationHistory history) {
        return BusLocationHistoryResponse.builder()
                .id(history.getId())
                .busId(history.getBus().getId())
                .tripId(history.getTrip() != null ? history.getTrip().getId() : null)
                .latitude(history.getLatitude())
                .longitude(history.getLongitude())
                .speed(history.getSpeed())
                .accuracy(history.getAccuracy())
                .timestamp(history.getTimestamp())
                .build();
    }
}
