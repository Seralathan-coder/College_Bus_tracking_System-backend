package com.college.bustracking.service;

import org.springframework.stereotype.Service;

@Service
public class EtaService {

    public int calculateEtaMinutes(double distanceKm, Double speedKmh) {
        double speed = (speedKmh == null || speedKmh <= 0)
                ? SpeedCalculationService.FALLBACK_SPEED_KMH
                : speedKmh;
        return (int) Math.round((distanceKm / speed) * 60.0);
    }
}
