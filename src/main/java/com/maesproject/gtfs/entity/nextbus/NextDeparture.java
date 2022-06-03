package com.maesproject.gtfs.entity.nextbus;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
public class NextDeparture {
    private String stopName;
    private String stopCode;
    private String routeShortName;
    private int directionId;
    private List<DepartureTrip> departureTrips;
}
