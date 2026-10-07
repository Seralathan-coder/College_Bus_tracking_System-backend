package com.college.bustracking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpeedCalculationServiceTest {

    private SpeedCalculationService service;

    @BeforeEach
    void setUp() {
        service = new SpeedCalculationService(new DistanceService());
    }

    @Test
    void fallbackWhenFewerThanTwoPoints() {
        assertEquals(20.0, service.movingAverageKmh(List.of()), 0.01);
    }

    @Test
    void movingAverageUsesRecentSegments() {
        Instant t0 = Instant.parse("2026-09-28T10:00:00Z");
        SpeedCalculationService.GpsPoint a = new SpeedCalculationService.GpsPoint(11.275, 77.605, t0);
        SpeedCalculationService.GpsPoint b = new SpeedCalculationService.GpsPoint(11.284, 77.605, t0.plusSeconds(180));
        double speed = service.movingAverageKmh(List.of(a, b));
        assertEquals(20.0, speed, 3.0);
    }

    @Test
    void rejectsUnrealisticGpsJump() {
        Instant t0 = Instant.parse("2026-09-28T10:00:00Z");
        SpeedCalculationService.GpsPoint erode = new SpeedCalculationService.GpsPoint(11.3410, 77.7172, t0);
        SpeedCalculationService.GpsPoint chennai = new SpeedCalculationService.GpsPoint(13.0827, 80.2707, t0.plusSeconds(5));
        assertTrue(service.isUnrealistic(erode, chennai));
        double speed = service.recordAndCalculate(1L, erode.getLatitude(), erode.getLongitude(), erode.getTimestamp());
        double afterJump = service.recordAndCalculate(1L, chennai.getLatitude(), chennai.getLongitude(), chennai.getTimestamp());
        assertEquals(speed, afterJump, 0.01);
        assertEquals(1, service.getRecentPoints(1L).size());
    }

    @Test
    void acceptsReasonableMovement() {
        Instant t0 = Instant.parse("2026-09-28T10:00:00Z");
        service.recordAndCalculate(9L, 11.275, 77.605, t0);
        double speed = service.recordAndCalculate(9L, 11.276, 77.605, t0.plusSeconds(10));
        assertTrue(speed < SpeedCalculationService.MAX_REASONABLE_SPEED_KMH);
        assertFalse(speed <= 0);
    }
}
