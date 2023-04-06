package com.maesproject.gtfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BusSchedule {
    private String routeShortName;
    private int directionId;
    private String dateCheck;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private List<RouteDirection> routeDirections;
    private List<StopSchedule> stopSchedules;
    private List<AlertInfo> alerts;

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

    @Data
    @NoArgsConstructor
    public static class AlertInfo {
        private String effect;
        private String duration;
        private String header;
    }
}
