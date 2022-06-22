package com.maesproject.gtfs.service.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class StartUpService {
    @Autowired
    private ThreadService threadService;
    @Autowired
    private ScheduleService scheduleService;

    @Value("${url.trip-update}")
    private String urlTripUpdate;
    @Value("${url.vehicle-position}")
    private String urlVehiclePosition;
    @Value("${url.alert}")
    private String urlAlert;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void printRunningMessage() {
        System.out.println("\nGTFS-Realtime-Consumer is running...\n");
    }

}
