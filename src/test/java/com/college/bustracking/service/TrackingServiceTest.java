package com.college.bustracking.service;

import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.Stop;
import com.college.bustracking.repository.BusLocationHistoryRepository;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.StopRepository;
import com.college.bustracking.repository.StudentStopRepository;
import com.college.bustracking.repository.TripRepository;
import com.college.bustracking.websocket.TrackingPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock private BusRepository busRepository;
    @Mock private StopRepository stopRepository;
    @Mock private StudentStopRepository studentStopRepository;
    @Mock private TripRepository tripRepository;
    @Mock private BusLocationHistoryRepository historyRepository;
    @Mock private TrackingPublisher trackingPublisher;

    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        DistanceService distanceService = new DistanceService();
        trackingService = new TrackingService(
                busRepository,
                stopRepository,
                studentStopRepository,
                tripRepository,
                historyRepository,
                distanceService,
                new SpeedCalculationService(distanceService),
                new EtaService(),
                trackingPublisher
        );
    }

    @Test
    void considersStopArrivedWithin100Meters() {
        assertTrue(trackingService.isWithinArrival(11.2750, 77.6050, 11.2754, 77.6050));
    }

    @Test
    void doesNotArriveWhenFarFromStop() {
        assertFalse(trackingService.isWithinArrival(11.2750, 77.6050, 11.2900, 77.6050));
    }

    @Test
    void fallbackEtaExampleMatchesSpec() {
        Bus bus = Bus.builder().currentLat(11.275).currentLng(77.605).currentSpeed(null).build();
        Stop stop = Stop.builder().latitude(11.275).longitude(77.605).stopName("Perundurai").build();
        assertTrue(bus.getCurrentSpeed() == null);
        assertTrue(stop.getStopName().equals("Perundurai"));
        assertTrue(new EtaService().calculateEtaMinutes(4.0, bus.getCurrentSpeed()) == 12);
    }
}
