package com.college.bustracking.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EtaServiceTest {

    private final EtaService etaService = new EtaService();

    @Test
    void fourKmAtTwentyKmhIsTwelveMinutes() {
        assertEquals(12, etaService.calculateEtaMinutes(4.0, 20.0));
    }

    @Test
    void usesFallbackSpeedWhenMissing() {
        assertEquals(12, etaService.calculateEtaMinutes(4.0, null));
        assertEquals(12, etaService.calculateEtaMinutes(4.0, 0.0));
    }
}
