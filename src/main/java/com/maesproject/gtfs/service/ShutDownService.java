package com.maesproject.gtfs.service;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class ShutDownService {

    @PreDestroy
    public void printShutDownMessage() {
        System.out.println("\nGTFS-Realtime-Consumer is stopped!\n");
    }
}
