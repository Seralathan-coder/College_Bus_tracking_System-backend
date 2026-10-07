package com.college.bustracking.service;

import com.college.bustracking.dto.StopSuggestionResponse;
import com.college.bustracking.dto.SuggestStopRequest;
import com.college.bustracking.entity.*;
import com.college.bustracking.exception.ApiException;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.repository.StopRepository;
import com.college.bustracking.repository.StopSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class StopSuggestionService {
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final StopSuggestionRepository suggestionRepository;

    @Transactional
    public StopSuggestionResponse suggest(User student, Long routeId, SuggestStopRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Route not found"));
        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Route is not active");
        }
        StopSuggestion suggestion = suggestionRepository.save(StopSuggestion.builder()
                .stopName(request.getStopName().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .route(route)
                .student(student)
                .status(StopSuggestionStatus.PENDING)
                .build());
        return toResponse(suggestion);
    }

    @Transactional(readOnly = true)
    public List<StopSuggestionResponse> pending() {
        return suggestionRepository.findByStatusOrderByCreatedAtAsc(StopSuggestionStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public StopSuggestionResponse approve(Long suggestionId) {
        StopSuggestion suggestion = requirePending(suggestionId);
        Route route = suggestion.getRoute();
        List<Stop> existingStops = new ArrayList<>(stopRepository.findByRouteIdOrderBySequenceOrderAsc(route.getId()));
        Stop approvedStop = Stop.builder()
                .stopName(suggestion.getStopName())
                .latitude(suggestion.getLatitude())
                .longitude(suggestion.getLongitude())
                .route(route)
                .build();
        List<Stop> orderedStops = orderByNearestCoordinates(existingStops, approvedStop);

        // Move existing stops out of the final sequence range first. This avoids
        // transient duplicate order values in databases with a unique route/order key.
        int temporaryOrder = existingStops.stream().mapToInt(Stop::getSequenceOrder).max().orElse(0) + 1;
        for (Stop stop : existingStops) {
            stop.setSequenceOrder(temporaryOrder++);
        }
        stopRepository.saveAllAndFlush(existingStops);

        for (int index = 0; index < orderedStops.size(); index++) {
            orderedStops.get(index).setSequenceOrder(index + 1);
        }
        stopRepository.saveAllAndFlush(orderedStops);
        suggestion.setStatus(StopSuggestionStatus.APPROVED);
        return toResponse(suggestionRepository.save(suggestion));
    }

    /** Keep the first and last stops as route endpoints, then order intermediate
     * stops by their geographic progress from the origin toward the destination. */
    private List<Stop> orderByNearestCoordinates(List<Stop> existingStops, Stop approvedStop) {
        if (existingStops.size() < 2) {
            List<Stop> result = new ArrayList<>(existingStops);
            result.add(approvedStop);
            return result;
        }

        Stop start = existingStops.get(0);
        Stop end = existingStops.get(existingStops.size() - 1);
        List<Stop> remaining = new ArrayList<>(existingStops.subList(1, existingStops.size() - 1));
        remaining.add(approvedStop);
        double meanLatitude = Math.toRadians((start.getLatitude() + end.getLatitude()) / 2.0);
        double startX = start.getLongitude() * Math.cos(meanLatitude);
        double startY = start.getLatitude();
        double directionX = end.getLongitude() * Math.cos(meanLatitude) - startX;
        double directionY = end.getLatitude() - startY;
        double directionLengthSquared = directionX * directionX + directionY * directionY;

        remaining.sort((first, second) -> Double.compare(
                routeProgress(first, startX, startY, directionX, directionY, directionLengthSquared, meanLatitude),
                routeProgress(second, startX, startY, directionX, directionY, directionLengthSquared, meanLatitude)));
        List<Stop> result = new ArrayList<>(existingStops.size() + 1);
        result.add(start);
        result.addAll(remaining);
        result.add(end);
        return result;
    }

    private double routeProgress(Stop stop, double startX, double startY, double directionX,
                                 double directionY, double directionLengthSquared, double meanLatitude) {
        if (directionLengthSquared == 0) return 0;
        double stopX = stop.getLongitude() * Math.cos(meanLatitude);
        double stopY = stop.getLatitude();
        return ((stopX - startX) * directionX + (stopY - startY) * directionY) / directionLengthSquared;
    }

    @Transactional
    public StopSuggestionResponse reject(Long suggestionId) {
        StopSuggestion suggestion = requirePending(suggestionId);
        suggestion.setStatus(StopSuggestionStatus.REJECTED);
        return toResponse(suggestionRepository.save(suggestion));
    }

    private StopSuggestion requirePending(Long id) {
        StopSuggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stop suggestion not found"));
        if (suggestion.getStatus() != StopSuggestionStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Stop suggestion has already been reviewed");
        }
        return suggestion;
    }

    private StopSuggestionResponse toResponse(StopSuggestion suggestion) {
        return StopSuggestionResponse.builder()
                .id(suggestion.getId())
                .stopName(suggestion.getStopName())
                .latitude(suggestion.getLatitude())
                .longitude(suggestion.getLongitude())
                .routeId(suggestion.getRoute().getId())
                .routeName(suggestion.getRoute().getRouteName())
                .busNumber(suggestion.getRoute().getBus().getBusNumber())
                .studentId(suggestion.getStudent().getId())
                .studentName(suggestion.getStudent().getName())
                .status(suggestion.getStatus())
                .createdAt(suggestion.getCreatedAt())
                .build();
    }
}
