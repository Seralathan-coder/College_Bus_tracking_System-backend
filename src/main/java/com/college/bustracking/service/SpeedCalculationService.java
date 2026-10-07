package com.college.bustracking.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpeedCalculationService {

    public static final double MAX_REASONABLE_SPEED_KMH = 100.0;
    public static final double FALLBACK_SPEED_KMH = 20.0;
    private static final int WINDOW_SIZE = 5;

    private final DistanceService distanceService;
    private final Map<Long, Deque<GpsPoint>> recentPoints = new ConcurrentHashMap<>();

    public SpeedCalculationService(DistanceService distanceService) {
        this.distanceService = distanceService;
    }

    public boolean isUnrealistic(GpsPoint previous, GpsPoint current) {
        if (previous == null) {
            return false;
        }
        double implied = calculateSegmentSpeedKmh(previous, current);
        return implied > MAX_REASONABLE_SPEED_KMH;
    }

    public double calculateSegmentSpeedKmh(GpsPoint from, GpsPoint to) {
        double hours = Math.max(Duration.between(from.getTimestamp(), to.getTimestamp()).toMillis() / 3_600_000.0, 1.0 / 3600.0);
        double distance = distanceService.calculateDistanceKm(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
        return distance / hours;
    }

    public double movingAverageKmh(List<GpsPoint> points) {
        if (points == null || points.size() < 2) {
            return FALLBACK_SPEED_KMH;
        }
        List<Double> speeds = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            GpsPoint previous = points.get(i - 1);
            GpsPoint current = points.get(i);
            if (isUnrealistic(previous, current)) {
                continue;
            }
            speeds.add(calculateSegmentSpeedKmh(previous, current));
        }
        if (speeds.isEmpty()) {
            return FALLBACK_SPEED_KMH;
        }
        return speeds.stream().mapToDouble(Double::doubleValue).average().orElse(FALLBACK_SPEED_KMH);
    }

    public double recordAndCalculate(Long busId, double latitude, double longitude, Instant timestamp) {
        GpsPoint incoming = new GpsPoint(latitude, longitude, timestamp);
        Deque<GpsPoint> window = recentPoints.computeIfAbsent(busId, id -> new ArrayDeque<>());
        GpsPoint last = window.peekLast();
        if (isUnrealistic(last, incoming)) {
            return lastSpeedOrFallback(window);
        }
        window.addLast(incoming);
        while (window.size() > WINDOW_SIZE) {
            window.removeFirst();
        }
        return movingAverageKmh(List.copyOf(window));
    }

    public List<GpsPoint> getRecentPoints(Long busId) {
        Deque<GpsPoint> window = recentPoints.get(busId);
        if (window == null) {
            return List.of();
        }
        return List.copyOf(window);
    }

    public void clear(Long busId) {
        recentPoints.remove(busId);
    }

    private double lastSpeedOrFallback(Deque<GpsPoint> window) {
        if (window.size() < 2) {
            return FALLBACK_SPEED_KMH;
        }
        return movingAverageKmh(List.copyOf(window));
    }

    @Getter
    @AllArgsConstructor
    public static class GpsPoint {
        private final double latitude;
        private final double longitude;
        private final Instant timestamp;
    }
}
