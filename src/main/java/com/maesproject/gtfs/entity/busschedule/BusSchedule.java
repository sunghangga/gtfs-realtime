package com.maesproject.gtfs.entity.busschedule;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BusSchedule {
    private String routeShortName;
    private int directionId;
    private String dateCheck;
    private String startTime;
    private String endTime;
    private List<RouteDirection> routeDirections;
    private List<StopSchedule> stopSchedules;

    @Data
    @NoArgsConstructor
    public static class RouteDirection {
        private int directionId;
        private String directionName;
    }

    @Data
    @NoArgsConstructor
    public static class StopSchedule {
        private String stopCode;
        private String stopName;
        private List<String> arrivalTimes;
    }
}
