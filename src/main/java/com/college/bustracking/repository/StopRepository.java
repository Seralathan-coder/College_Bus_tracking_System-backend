package com.college.bustracking.repository;

import com.college.bustracking.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {
    @Query("select s from Stop s join fetch s.route where s.route.id = :routeId order by s.sequenceOrder asc")
    List<Stop> findByRouteIdOrderBySequenceOrderAsc(@Param("routeId") Long routeId);

    List<Stop> findAllByRouteIdOrderBySequenceOrderAsc(Long routeId);
}
