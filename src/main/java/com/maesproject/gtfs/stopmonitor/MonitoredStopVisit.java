package com.maesproject.gtfs.stopmonitor;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class MonitoredStopVisit {
    private LocalDateTime RecordedAtTime;
    private String MonitoringRef;
    private MonitoredVehicleJourney monitoredVehicleJourney;
}
