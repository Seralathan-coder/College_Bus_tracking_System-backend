package com.college.bustracking.repository;

import com.college.bustracking.entity.Trip;
import com.college.bustracking.entity.TripStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Override
    @EntityGraph(attributePaths = {"bus", "route", "driver"})
    Optional<Trip> findById(Long id);

    @EntityGraph(attributePaths = {"bus", "route", "driver"})
    Optional<Trip> findFirstByBusIdAndStatusInOrderByStartedAtDesc(Long busId, List<TripStatus> statuses);

    @EntityGraph(attributePaths = {"bus", "route", "driver"})
    Optional<Trip> findFirstByDriverIdAndStatusInOrderByStartedAtDesc(Long driverId, List<TripStatus> statuses);

    @EntityGraph(attributePaths = {"bus", "route", "driver"})
    @Query("select t from Trip t where t.id = :tripId")
    Optional<Trip> findDetailedById(@Param("tripId") Long tripId);
    List<Trip> findByDriverIdOrderByStartedAtDesc(Long driverId);
    List<Trip> findByBusIdOrderByStartedAtDesc(Long busId);
    long countByStatus(TripStatus status);
    boolean existsByRouteId(Long routeId);

    @EntityGraph(attributePaths = {"bus", "route", "driver"})
    List<Trip> findAll();
}
