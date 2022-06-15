package com.maesproject.gtfs.entity.busschedule;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BusSchedule {
    private List<StopSchedule> stopSchedules;

    @Data
    @NoArgsConstructor
    public static class StopSchedule {
        private String stopCode;
        private String stopName;
        private List<String> arrivalTimes;
    }
}
