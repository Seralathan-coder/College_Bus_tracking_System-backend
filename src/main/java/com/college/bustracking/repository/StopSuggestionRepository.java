package com.college.bustracking.repository;

import com.college.bustracking.entity.StopSuggestion;
import com.college.bustracking.entity.StopSuggestionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StopSuggestionRepository extends JpaRepository<StopSuggestion, Long> {
    @EntityGraph(attributePaths = {"route", "route.bus", "student"})
    List<StopSuggestion> findByStatusOrderByCreatedAtAsc(StopSuggestionStatus status);

    @Override
    @EntityGraph(attributePaths = {"route", "route.bus", "student"})
    Optional<StopSuggestion> findById(Long id);
}
