package com.maesproject.gtfs.compositeid;

import java.io.Serializable;

public class StopMonitoringCompositeId implements Serializable {
    private String agencyId;
    private String routeId;
    private String tripId;
    private int directionId;
    private String stopId;
    private int stopSequence;

    public StopMonitoringCompositeId() {
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
