package com.college.bustracking.repository;

import com.college.bustracking.entity.Route;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    @EntityGraph(attributePaths = {"driver", "bus", "stops"})
    List<Route> findByDriverId(Long driverId);

    @EntityGraph(attributePaths = {"driver", "bus", "stops"})
    List<Route> findByBusId(Long busId);

    Optional<Route> findFirstByBusIdAndStatusOrderByUpdatedAtDesc(Long busId, com.college.bustracking.entity.RouteStatus status);

    @EntityGraph(attributePaths = {"driver", "bus", "stops"})
    List<Route> findAll();
}
