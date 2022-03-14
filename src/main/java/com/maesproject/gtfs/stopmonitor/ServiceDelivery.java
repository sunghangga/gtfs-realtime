package com.maesproject.gtfs.stopmonitor;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceDelivery {
    private LocalDateTime ResponseTimestamp;
    private String ProducerRef;
    private boolean Status;
    private StopMonitoringDelivery StopMonitoringDelivery;
}
