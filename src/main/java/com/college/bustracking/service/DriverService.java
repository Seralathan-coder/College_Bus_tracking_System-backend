package com.college.bustracking.service;

import com.college.bustracking.dto.CreateRouteRequest;
import com.college.bustracking.dto.CreateStopRequest;
import com.college.bustracking.dto.StartTripRequest;
import com.college.bustracking.dto.SaveRouteContentsRequest;
import com.college.bustracking.dto.RouteStopItemRequest;
import com.college.bustracking.dto.UpdateRouteRequest;
import com.college.bustracking.dto.UpdateStopRequest;
import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusStatus;
import com.college.bustracking.entity.Route;
import com.college.bustracking.entity.RouteStatus;
import com.college.bustracking.entity.Stop;
import com.college.bustracking.entity.Trip;
import com.college.bustracking.entity.TripStatus;
import com.college.bustracking.entity.User;
import com.college.bustracking.exception.ApiException;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.repository.StopRepository;
import com.college.bustracking.repository.StudentStopRepository;
import com.college.bustracking.repository.TripRepository;
import com.college.bustracking.websocket.TrackingPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final StudentStopRepository studentStopRepository;
    private final TripRepository tripRepository;
    private final TrackingPublisher trackingPublisher;

    public Bus requireAssignedBus(User driver) {
        return busRepository.findByDriverId(driver.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No bus assigned to this driver"));
    }

    private void assertOwnsBus(User driver, Bus bus) {
        if (bus.getDriver() == null || !bus.getDriver().getId().equals(driver.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your assigned bus");
        }
    }

    private Route requireOwnedRoute(User driver, Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Route not found"));
        if (!route.getDriver().getId().equals(driver.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own routes");
        }
        return route;
    }

    @Transactional
    public Route createRoute(User driver, CreateRouteRequest request) {
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found"));
        assertOwnsBus(driver, bus);
        Route route = Route.builder()
                .routeName(request.getRouteName())
                .driver(driver)
                .bus(bus)
                .status(RouteStatus.ACTIVE)
                .build();
        if (request.getStops() != null) {
            request.getStops().stream()
                    .sorted(Comparator.comparing(CreateStopRequest::getSequenceOrder))
                    .forEach(stopRequest -> route.getStops().add(Stop.builder()
                            .stopName(stopRequest.getStopName())
                            .latitude(stopRequest.getLatitude())
                            .longitude(stopRequest.getLongitude())
                            .sequenceOrder(stopRequest.getSequenceOrder())
                            .route(route)
                            .build()));
        }
        return routeRepository.save(route);
    }

    @Transactional
    public Route updateRoute(User driver, Long routeId, UpdateRouteRequest request) {
        Route route = requireOwnedRoute(driver, routeId);
        if (request.getRouteName() != null) {
            route.setRouteName(request.getRouteName());
        }
        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
        }
        return routeRepository.save(route);
    }

    public List<Route> myRoutes(User driver) {
        return routeRepository.findByDriverId(driver.getId()).stream()
                .filter(route -> route.getStatus() != RouteStatus.INACTIVE)
                .toList();
    }

    @Transactional
    public Route saveRouteContents(User driver, Long routeId, SaveRouteContentsRequest request) {
        Route route = requireOwnedRoute(driver, routeId);
        List<Stop> existingStops = stopRepository.findAllByRouteIdOrderBySequenceOrderAsc(routeId);
        Map<Long, Stop> existingById = new HashMap<>();
        existingStops.forEach(stop -> existingById.put(stop.getId(), stop));

        Set<Long> retainedIds = new HashSet<>();
        for (RouteStopItemRequest item : request.getStops()) {
            if (item.getId() == null) continue;
            if (!retainedIds.add(item.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "A stop cannot appear more than once");
            }
            if (!existingById.containsKey(item.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "A selected stop does not belong to this route");
            }
        }

        route.setRouteName(request.getRouteName().trim());
        routeRepository.save(route);

        List<Stop> removedStops = existingStops.stream()
                .filter(stop -> !retainedIds.contains(stop.getId())).toList();
        for (Stop stop : removedStops) {
            studentStopRepository.deleteByStopId(stop.getId());
        }
        stopRepository.deleteAll(removedStops);
        stopRepository.flush();

        // Move retained rows out of the target order range before assigning the new
        // order. This also works when the database enforces unique(route_id, sequence_order).
        int temporaryOrder = existingStops.stream().mapToInt(Stop::getSequenceOrder).max().orElse(0) + 1;
        for (int i = 0; i < request.getStops().size(); i++) {
            RouteStopItemRequest item = request.getStops().get(i);
            if (item.getId() == null) continue;
            Stop stop = existingById.get(item.getId());
            stop.setSequenceOrder(temporaryOrder++);
        }
        stopRepository.saveAllAndFlush(retainedIds.stream().map(existingById::get).toList());

        for (int i = 0; i < request.getStops().size(); i++) {
            RouteStopItemRequest item = request.getStops().get(i);
            Stop stop = item.getId() == null
                    ? Stop.builder().route(route).build()
                    : existingById.get(item.getId());
            stop.setStopName(item.getStopName().trim());
            stop.setLatitude(item.getLatitude());
            stop.setLongitude(item.getLongitude());
            stop.setSequenceOrder(i + 1);
            stop.setRoute(route);
            stopRepository.save(stop);
        }

        return routeRepository.findByDriverId(driver.getId()).stream()
                .filter(savedRoute -> savedRoute.getId().equals(routeId))
                .findFirst().orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Route not found"));
    }

    @Transactional
    public void deleteRoute(User driver, Long routeId) {
        Route route = requireOwnedRoute(driver, routeId);
        // Keep the route and its stops when they are part of trip history.
        if (tripRepository.existsByRouteId(routeId)) {
            route.setStatus(RouteStatus.INACTIVE);
            routeRepository.save(route);
        } else {
            routeRepository.delete(route);
        }
    }

    @Transactional
    public Stop addStop(User driver, CreateStopRequest request) {
        if (request.getRouteId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "routeId is required");
        }
        Route route = requireOwnedRoute(driver, request.getRouteId());
        Stop stop = Stop.builder()
                .stopName(request.getStopName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .sequenceOrder(request.getSequenceOrder())
                .route(route)
                .build();
        return stopRepository.save(stop);
    }

    @Transactional
    public Stop updateStop(User driver, Long stopId, UpdateStopRequest request) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stop not found"));
        requireOwnedRoute(driver, stop.getRoute().getId());
        if (request.getStopName() != null) {
            stop.setStopName(request.getStopName());
        }
        if (request.getLatitude() != null) {
            stop.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            stop.setLongitude(request.getLongitude());
        }
        if (request.getSequenceOrder() != null) {
            stop.setSequenceOrder(request.getSequenceOrder());
        }
        return stopRepository.save(stop);
    }

    @Transactional
    public void deleteStop(User driver, Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stop not found"));
        requireOwnedRoute(driver, stop.getRoute().getId());
        studentStopRepository.deleteByStopId(stopId);
        stopRepository.delete(stop);
    }

    @Transactional
    public Trip startTrip(User driver, StartTripRequest request) {
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found"));
        assertOwnsBus(driver, bus);
        tripRepository.findFirstByBusIdAndStatusInOrderByStartedAtDesc(bus.getId(),
                        List.of(TripStatus.ACTIVE, TripStatus.PAUSED))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "An active trip already exists for this bus");
                });
        Route route = requireOwnedRoute(driver, request.getRouteId());
        if (!route.getBus().getId().equals(bus.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Route does not belong to this bus");
        }
        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The selected route is not active");
        }
        Trip trip = tripRepository.save(Trip.builder()
                .bus(bus)
                .route(route)
                .driver(driver)
                .startedAt(Instant.now())
                .status(TripStatus.ACTIVE)
                .build());
        bus.setStatus(BusStatus.ACTIVE);
        busRepository.save(bus);
        trackingPublisher.publishStatus(bus.getId(), BusStatus.ACTIVE);
        return trip;
    }

    @Transactional
    public Trip pauseTrip(User driver, Long tripId) {
        Trip trip = requireOwnTrip(driver, tripId);
        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only an active trip can be paused");
        }
        trip.setStatus(TripStatus.PAUSED);
        trip.getBus().setStatus(BusStatus.PAUSED);
        busRepository.save(trip.getBus());
        trackingPublisher.publishStatus(trip.getBus().getId(), BusStatus.PAUSED);
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip resumeTrip(User driver, Long tripId) {
        Trip trip = requireOwnTrip(driver, tripId);
        if (trip.getStatus() != TripStatus.PAUSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only a paused trip can be resumed");
        }
        trip.setStatus(TripStatus.ACTIVE);
        trip.getBus().setStatus(BusStatus.ACTIVE);
        busRepository.save(trip.getBus());
        trackingPublisher.publishStatus(trip.getBus().getId(), BusStatus.ACTIVE);
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip endTrip(User driver, Long tripId) {
        Trip trip = requireOwnTrip(driver, tripId);
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trip already ended");
        }
        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndedAt(Instant.now());
        trip.getBus().setStatus(BusStatus.IDLE);
        busRepository.save(trip.getBus());
        trackingPublisher.publishStatus(trip.getBus().getId(), BusStatus.IDLE);
        return tripRepository.save(trip);
    }

    public java.util.Optional<Trip> currentTrip(User driver) {
        return tripRepository.findFirstByDriverIdAndStatusInOrderByStartedAtDesc(driver.getId(),
                List.of(TripStatus.ACTIVE, TripStatus.PAUSED));
    }

    private Trip requireOwnTrip(User driver, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getDriver().getId().equals(driver.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own trips");
        }
        return trip;
    }
}
