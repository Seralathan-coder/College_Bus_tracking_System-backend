package com.college.bustracking.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceServiceTest {

    private final DistanceService service = new DistanceService();

    @Test
    void sameCoordinatesAreZero() {
        assertEquals(0.0, service.calculateDistanceKm(11.275, 77.605, 11.275, 77.605), 0.0001);
    }

    @Test
    void shortDistanceIsAboutOneKilometer() {
        double distance = service.calculateDistanceKm(11.275, 77.605, 11.284, 77.605);
        assertEquals(1.0, distance, 0.05);
    }

    @Test
    void longDistanceErodeToChennai() {
        double distance = service.calculateDistanceKm(11.3410, 77.7172, 13.0827, 80.2707);
        assertEquals(320.0, distance, 40.0);
    }
}
