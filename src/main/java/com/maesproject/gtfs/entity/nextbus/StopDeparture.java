package com.maesproject.gtfs.entity.nextbus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class StopDeparture {
    private String stopName;
    private String stopCode;
    private String routeShortName;
    private String routeLongName;
    private int directionId;
    private List<DepartureSchedule> departureSchedules;

    @Data
    @AllArgsConstructor
    public static class DepartureSchedule {
        private String tripHeadSign;
        private String departing;
        private String next;
    }
}
