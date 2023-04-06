package com.maesproject.gtfs.service.app;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class StartUpService {
    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void printRunningMessage() {
        System.out.println("\nGTFS-Realtime-Consumer is running...\n");
    }

}
