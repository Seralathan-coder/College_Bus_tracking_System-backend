package com.college.bustracking.service;

import com.college.bustracking.entity.Stop;
import com.college.bustracking.entity.RouteStatus;
import com.college.bustracking.exception.ApiException;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.repository.StopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusStopQueryService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    public BusStopQueryService(BusRepository busRepository, RouteRepository routeRepository, StopRepository stopRepository) {
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
    }

    @Transactional(readOnly = true)
    public List<Stop> stopsForBus(Long busId) {
        if (!busRepository.existsById(busId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Bus not found");
        }
        return routeRepository.findByBusId(busId).stream()
                .filter(route -> route.getStatus() == RouteStatus.ACTIVE)
                .flatMap(route -> stopRepository.findByRouteIdOrderBySequenceOrderAsc(route.getId()).stream())
                .toList();
    }
}
