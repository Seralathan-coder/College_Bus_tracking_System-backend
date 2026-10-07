package com.college.bustracking.service;

import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusStatus;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.websocket.TrackingPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfflineBusService {

    private final BusRepository busRepository;
    private final TrackingPublisher trackingPublisher;

    @Scheduled(fixedDelay = 10000)
    public void markOfflineBuses() {
        Instant cutoff = Instant.now().minus(Duration.ofSeconds(30));
        List<Bus> buses = busRepository.findAll();
        for (Bus bus : buses) {
            if (bus.getStatus() == BusStatus.ACTIVE
                    && bus.getLastUpdated() != null
                    && bus.getLastUpdated().isBefore(cutoff)) {
                bus.setStatus(BusStatus.OFFLINE);
                busRepository.save(bus);
                trackingPublisher.publishStatus(bus.getId(), BusStatus.OFFLINE);
            }
        }
    }
}
