package com.maesproject.gtfs.entity.nextbus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DestinationTrip {
    private String combinedTripHeadSign;
    private int directionId;
}
