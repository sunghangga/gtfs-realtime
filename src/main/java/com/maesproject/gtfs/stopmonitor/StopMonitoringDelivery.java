package com.maesproject.gtfs.stopmonitor;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StopMonitoringDelivery {
    private String version;
    private LocalDateTime ResponseTimestamp;
    private boolean status;
    List<MonitoredStopVisit> monitoredStopVisits;
    List<MonitoredStopVisitCancellation> monitoredStopVisitCancellations;
}
