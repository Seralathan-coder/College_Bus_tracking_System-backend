package com.college.bustracking.repository;

import com.college.bustracking.entity.BusLocationHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusLocationHistoryRepository extends JpaRepository<BusLocationHistory, Long> {
    @EntityGraph(attributePaths = {"bus", "trip"})
    List<BusLocationHistory> findByBusIdOrderByTimestampDesc(Long busId);
    List<BusLocationHistory> findTop20ByBusIdOrderByTimestampDesc(Long busId);
    List<BusLocationHistory> findByTripIdOrderByTimestampAsc(Long tripId);
}
