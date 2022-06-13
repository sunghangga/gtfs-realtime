package com.maesproject.gtfs.entity.nextbus;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Destination {
    private String routeShortName;
    private String routeLongName;
    private List<DestinationTrip> destinationTrips;

    @Data
    @NoArgsConstructor
    public static class DestinationTrip {
        private String combinedTripHeadSign;
        private int directionId;
    }
}
