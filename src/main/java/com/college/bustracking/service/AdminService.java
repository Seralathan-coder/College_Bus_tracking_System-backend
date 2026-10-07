package com.college.bustracking.service;

import com.college.bustracking.dto.CreateBusRequest;
import com.college.bustracking.dto.DashboardResponse;
import com.college.bustracking.dto.UpdateBusRequest;
import com.college.bustracking.dto.UpdateStopCoordinatesRequest;
import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusStatus;
import com.college.bustracking.entity.Role;
import com.college.bustracking.entity.Stop;
import com.college.bustracking.entity.TripStatus;
import com.college.bustracking.entity.User;
import com.college.bustracking.exception.ApiException;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.TripRepository;
import com.college.bustracking.repository.StopRepository;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;

    public DashboardResponse dashboard() {
        return DashboardResponse.builder()
                .totalBuses(busRepository.count())
                .activeBuses(busRepository.countByStatus(BusStatus.ACTIVE))
                .offlineBuses(busRepository.countByStatus(BusStatus.OFFLINE))
                .drivers(userRepository.countByRole(Role.DRIVER))
                .students(userRepository.countByRole(Role.STUDENT))
                .activeTrips(tripRepository.countByStatus(TripStatus.ACTIVE))
                .build();
    }

    public List<User> users() {
        return userRepository.findAll();
    }

    public List<User> drivers() {
        return userRepository.findByRole(Role.DRIVER);
    }

    public List<User> students() {
        return userRepository.findByRole(Role.STUDENT);
    }

    @Transactional
    public Bus createBus(CreateBusRequest request) {
        if (busRepository.existsByBusNumber(request.getBusNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "Bus number already exists");
        }
        Bus bus = Bus.builder()
                .busNumber(request.getBusNumber())
                .status(BusStatus.IDLE)
                .build();
        if (request.getDriverId() != null) {
            bus.setDriver(requireDriver(request.getDriverId(), null));
        }
        return busRepository.save(bus);
    }

    @Transactional
    public Bus updateBus(Long id, UpdateBusRequest request) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found"));
        if (request.getBusNumber() != null && !request.getBusNumber().equals(bus.getBusNumber())) {
            if (busRepository.existsByBusNumber(request.getBusNumber())) {
                throw new ApiException(HttpStatus.CONFLICT, "Bus number already exists");
            }
            bus.setBusNumber(request.getBusNumber());
        }
        if (request.getDriverId() != null) {
            bus.setDriver(requireDriver(request.getDriverId(), bus.getId()));
        }
        return busRepository.save(bus);
    }

    @Transactional
    public void deleteBus(Long id) {
        if (!busRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Bus not found");
        }
        busRepository.deleteById(id);
    }

    @Transactional
    public Stop updateRouteStopCoordinates(Long routeId, Long stopId, UpdateStopCoordinatesRequest request) {
        if (!routeRepository.existsById(routeId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Route not found");
        }
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stop not found"));
        if (!stop.getRoute().getId().equals(routeId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stop does not belong to this route");
        }
        stop.setLatitude(request.getLatitude());
        stop.setLongitude(request.getLongitude());
        return stopRepository.save(stop);
    }

    private User requireDriver(Long driverId, Long currentBusId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found"));
        if (driver.getRole() != Role.DRIVER) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned user is not a driver");
        }
        busRepository.findByDriverId(driverId).ifPresent(existing -> {
            if (currentBusId == null || !existing.getId().equals(currentBusId)) {
                throw new ApiException(HttpStatus.CONFLICT, "Driver already assigned to bus " + existing.getBusNumber());
            }
        });
        return driver;
    }
}
