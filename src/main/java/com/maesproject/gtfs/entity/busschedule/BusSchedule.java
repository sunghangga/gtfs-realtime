package com.maesproject.gtfs.entity.busschedule;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class BusSchedule {
    private List<StopSchedule> stopSchedules;
}
