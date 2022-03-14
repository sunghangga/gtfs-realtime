package com.maesproject.gtfs.stopmonitor;

import java.time.LocalDateTime;

public class MonitoredStopVisitCancellation {
    private LocalDateTime RecordedAtTime;
    private String MonitoringRef;
    private String LineRef;
    private String DirectionRef;
    private FramedVehicleJourneyRef framedVehicleJourneyRef;
}
