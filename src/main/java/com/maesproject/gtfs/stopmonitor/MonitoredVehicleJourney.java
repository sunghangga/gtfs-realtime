package com.maesproject.gtfs.stopmonitor;

import lombok.Data;

@Data
public class MonitoredVehicleJourney {
    private String LineRef;
    private int DirectionRef;
    private FramedVehicleJourneyRef framedVehicleJourneyRef;
    private String PublishedLineName;
    private String OperatorRef;
    private String OriginRef;
    private String OriginName;
    private String DestinationRef;
    private String DestinationName;
    private boolean Monitored;
    private String InCongestion;
    private VehicleLocation vehicleLocation;
    private double Bearing;
    private String Occupancy;
    private String VehicleRef;
    private MonitoredCall monitoredCall;
}
