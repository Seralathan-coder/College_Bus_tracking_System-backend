package com.college.bustracking.service;

import com.college.bustracking.dto.EtaResponse;
import com.college.bustracking.dto.LocationUpdateRequest;
import com.college.bustracking.dto.StudentStopRequest;
import com.college.bustracking.dto.StudentStopResponse;
import com.college.bustracking.dto.TrackingResponse;
import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusLocationHistory;
import com.college.bustracking.entity.Role;
import com.college.bustracking.entity.Stop;
import com.college.bustracking.entity.StudentStop;
import com.college.bustracking.entity.Trip;
import com.college.bustracking.entity.TripStatus;
import com.college.bustracking.entity.User;
import com.college.bustracking.exception.ApiException;
import com.college.bustracking.repository.BusLocationHistoryRepository;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.StopRepository;
import com.college.bustracking.repository.StudentStopRepository;
import com.college.bustracking.repository.TripRepository;
import com.college.bustracking.websocket.TrackingPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingService {

    public static final double ARRIVAL_THRESHOLD_KM = 0.1;

    private final BusRepository busRepository;
    private final StopRepository stopRepository;
    private final StudentStopRepository studentStopRepository;
    private final TripRepository tripRepository;
    private final BusLocationHistoryRepository historyRepository;
    private final DistanceService distanceService;
    private final SpeedCalculationService speedCalculationService;
    private final EtaService etaService;
    private final TrackingPublisher trackingPublisher;

    @Transactional
    public TrackingResponse updateLocation(User actor, Long busId, LocationUpdateRequest request) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found"));
        if (actor.getRole() == Role.DRIVER && (bus.getDriver() == null || !bus.getDriver().getId().equals(actor.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Drivers can only send GPS for their assigned bus");
        }
        if (actor.getRole() == Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Students cannot send GPS");
        }
        Instant timestamp = request.getTimestamp() == null ? Instant.now() : request.getTimestamp();
        double speed = speedCalculationService.recordAndCalculate(busId, request.getLatitude(), request.getLongitude(), timestamp);
        bus.setCurrentLat(request.getLatitude());
        bus.setCurrentLng(request.getLongitude());
        bus.setCurrentSpeed(speed);
        bus.setLastUpdated(timestamp);
        busRepository.save(bus);

        Optional<Trip> activeTrip = tripRepository.findFirstByBusIdAndStatusInOrderByStartedAtDesc(
                busId, List.of(TripStatus.ACTIVE, TripStatus.PAUSED));
        historyRepository.save(BusLocationHistory.builder()
                .bus(bus)
                .trip(activeTrip.orElse(null))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(speed)
                .accuracy(request.getAccuracy())
                .timestamp(timestamp)
                .build());

        TrackingSnapshot snapshot = snapshot(bus, null);
        if (snapshot.arrivedStop != null) {
            trackingPublisher.publishArrival(bus.getId(), snapshot.arrivedStop.getStopName());
        }
        TrackingResponse response = toResponse(bus, snapshot, null);
        trackingPublisher.publishLocation(response);
        return response;
    }

    public TrackingResponse tracking(Long busId, User student) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found"));
        Long selectedStopId = studentStopRepository.findByStudentIdAndBusId(student.getId(), busId)
                .map(ss -> ss.getStop().getId())
                .orElse(null);
        return toResponse(bus, snapshot(bus, selectedStopId), selectedStopId);
    }

    public EtaResponse etaForStop(Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stop not found"));
        Bus bus = stop.getRoute().getBus();
        if (bus.getCurrentLat() == null || bus.getCurrentLng() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Bus location is not available yet");
        }
        double distance = distanceService.calculateDistanceKm(
                bus.getCurrentLat(), bus.getCurrentLng(), stop.getLatitude(), stop.getLongitude());
        double speed = bus.getCurrentSpeed() == null || bus.getCurrentSpeed() <= 0
                ? SpeedCalculationService.FALLBACK_SPEED_KMH
                : bus.getCurrentSpeed();
        TrackingSnapshot snapshot = snapshot(bus, stop.getId());
        return EtaResponse.builder()
                .stopId(stop.getId())
                .stopName(stop.getStopName())
                .distanceKm(round(distance))
                .speedKmh(round(speed))
                .etaMinutes(etaService.calculateEtaMinutes(distance, speed))
                .nextStop(snapshot.nextStop == null ? null : snapshot.nextStop.getStopName())
                .lastUpdated(bus.getLastUpdated())
                .build();
    }

    @Transactional
    public StudentStopResponse selectStop(User student, StudentStopRequest request) {
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found"));
        Stop stop = stopRepository.findById(request.getStopId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stop not found"));
        if (!stop.getRoute().getBus().getId().equals(bus.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stop does not belong to the selected bus");
        }
        StudentStop studentStop = studentStopRepository.findByStudentIdAndBusId(student.getId(), bus.getId())
                .orElse(StudentStop.builder().student(student).bus(bus).build());
        studentStop.setStop(stop);
        StudentStop saved = studentStopRepository.save(studentStop);
        return StudentStopResponse.builder()
                .id(saved.getId())
                .studentId(student.getId())
                .busId(bus.getId())
                .stopId(stop.getId())
                .stopName(stop.getStopName())
                .build();
    }

    public boolean isWithinArrival(double busLat, double busLng, double stopLat, double stopLng) {
        return distanceService.calculateDistanceKm(busLat, busLng, stopLat, stopLng) <= ARRIVAL_THRESHOLD_KM;
    }

    TrackingSnapshot snapshot(Bus bus, Long selectedStopId) {
        List<Stop> stops = List.of();
        Optional<Trip> trip = tripRepository.findFirstByBusIdAndStatusInOrderByStartedAtDesc(
                bus.getId(), List.of(TripStatus.ACTIVE, TripStatus.PAUSED));
        if (trip.isPresent()) {
            stops = stopRepository.findByRouteIdOrderBySequenceOrderAsc(trip.get().getRoute().getId());
        }
        Stop current = null;
        Stop next = null;
        Stop arrived = null;
        if (bus.getCurrentLat() != null && bus.getCurrentLng() != null) {
            for (int i = 0; i < stops.size(); i++) {
                Stop stop = stops.get(i);
                if (isWithinArrival(bus.getCurrentLat(), bus.getCurrentLng(), stop.getLatitude(), stop.getLongitude())) {
                    current = stop;
                    arrived = stop;
                    next = i + 1 < stops.size() ? stops.get(i + 1) : null;
                    break;
                }
            }
            if (next == null && current == null) {
                int minimumSequence = selectedStopId == null ? Integer.MIN_VALUE : findSequence(stops, selectedStopId);
                next = stops.stream()
                    .filter(stop -> stop.getSequenceOrder() >= minimumSequence)
                        .findFirst()
                        .orElse(stops.isEmpty() ? null : stops.get(0));
                if (next == null && !stops.isEmpty()) {
                    next = stops.get(0);
                }
            }
        }
        return new TrackingSnapshot(current, next, arrived);
    }

    private int findSequence(List<Stop> stops, Long stopId) {
        return stops.stream()
                .filter(stop -> stop.getId().equals(stopId))
                .map(Stop::getSequenceOrder)
                .findFirst()
                .orElse(1);
    }

    private TrackingResponse toResponse(Bus bus, TrackingSnapshot snapshot, Long selectedStopId) {
        Double distanceToNext = null;
        Integer eta = null;
        Double distanceToSelected = null;
        Stop selectedStop = selectedStopId == null ? null : stopRepository.findById(selectedStopId).orElse(null);
        if (bus.getCurrentLat() != null && bus.getCurrentLng() != null && snapshot.nextStop != null) {
            distanceToNext = round(distanceService.calculateDistanceKm(
                    bus.getCurrentLat(), bus.getCurrentLng(),
                    snapshot.nextStop.getLatitude(), snapshot.nextStop.getLongitude()));
        }
        if (selectedStop != null && bus.getCurrentLat() != null && bus.getCurrentLng() != null) {
            distanceToSelected = round(distanceService.calculateDistanceKm(
                    bus.getCurrentLat(), bus.getCurrentLng(), selectedStop.getLatitude(), selectedStop.getLongitude()));
        }
        Double etaDistance = distanceToSelected != null ? distanceToSelected : distanceToNext;
        if (etaDistance != null) {
            eta = etaService.calculateEtaMinutes(etaDistance, bus.getCurrentSpeed());
        }
        return TrackingResponse.builder()
                .busId(bus.getId())
                .busNumber(bus.getBusNumber())
                .latitude(bus.getCurrentLat())
                .longitude(bus.getCurrentLng())
                .speed(bus.getCurrentSpeed())
                .distanceToNextStop(distanceToNext)
                .distanceToSelectedStop(distanceToSelected)
                .etaMinutes(eta)
                .nextStop(snapshot.nextStop == null ? null : snapshot.nextStop.getStopName())
                .currentStop(snapshot.currentStop == null ? null : snapshot.currentStop.getStopName())
                .status(bus.getStatus())
                .timestamp(bus.getLastUpdated())
                .lastUpdated(bus.getLastUpdated())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    record TrackingSnapshot(Stop currentStop, Stop nextStop, Stop arrivedStop) {
    }
}
