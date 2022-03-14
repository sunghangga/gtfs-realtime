package com.maesproject.gtfs.service;

import com.maesproject.gtfs.util.GlobalVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class StartUpService implements GlobalVariable {
    @Autowired
    private ThreadService threadService;
    @Autowired
    private ScheduleService scheduleService;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void printRunningMessage() {
        System.out.println("\nGTFS-Realtime-Consumer is running...\n");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void startGtfsRealtimeConsumer() {
        String[] feedUrls = {
                URL_TRIP_UPDATE,
                URL_VEHICLE_POSITION,
                URL_ALERT
        };
        for (String url : feedUrls) {
            threadService.consumeFeed(url);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
    public void startDeleteScheduler() {
        scheduleService.deleteExpiredRealtimeData();
    }
}
