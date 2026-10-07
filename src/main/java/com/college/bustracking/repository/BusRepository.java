package com.college.bustracking.repository;

import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByBusNumber(String busNumber);
    boolean existsByBusNumber(String busNumber);
    @EntityGraph(attributePaths = "driver")
    Optional<Bus> findByDriverId(Long driverId);
    List<Bus> findByStatus(BusStatus status);
    long countByStatus(BusStatus status);

    @EntityGraph(attributePaths = "driver")
    List<Bus> findAll();
}
