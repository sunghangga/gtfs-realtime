package com.maesproject.gtfs.stopmonitor;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitoredCall {
    private String StopPointRef;
    private String StopPointName;
    private String VehicleLocationAtStop;
    private boolean VehicleAtStop;
    private String AimedArrivalTime;
    private LocalDateTime ExpectedArrivalTime;
    private String AimedDepartureTime;
    private LocalDateTime ExpectedDepartureTime;
    private String Distances;
}
