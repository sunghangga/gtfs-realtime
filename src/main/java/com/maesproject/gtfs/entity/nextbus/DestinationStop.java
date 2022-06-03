package com.maesproject.gtfs.entity.nextbus;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DestinationStop {
    private String routeShortName;
    private String combinedTripHeadSign;
    private int oppositeDirection;
    private List<StopCheck> stopChecks;
}
