package com.college.bustracking.websocket;

import com.college.bustracking.dto.TrackingResponse;
import com.college.bustracking.entity.BusStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TrackingPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishLocation(TrackingResponse payload) {
        messagingTemplate.convertAndSend("/topic/bus/" + payload.getBusId() + "/location", payload);
    }

    public void publishStatus(Long busId, BusStatus status) {
        messagingTemplate.convertAndSend("/topic/bus/" + busId + "/status",
                Map.of("busId", busId, "status", status.name()));
    }

    public void publishArrival(Long busId, String stopName) {
        messagingTemplate.convertAndSend("/topic/bus/" + busId + "/arrival",
                Map.of("busId", busId, "stopName", stopName, "event", "ARRIVED"));
    }
}
