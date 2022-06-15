package com.maesproject.gtfs.service;

import com.maesproject.gtfs.entity.busschedule.BusSchedule;
import com.maesproject.gtfs.entity.busschedule.StopSchedule;
import com.maesproject.gtfs.repository.BusScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusScheduleService {
    @Autowired
    private BusScheduleRepository busScheduleRepository;
    @Autowired
    private NextBusService nextBusService;

    public BusSchedule getBusSchedule(String routeShortName, int directionId, String dateCheck, String startTime, String endTime) {
        LocalDate date = LocalDate.parse(dateCheck);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        String dateWithoutDash = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = date.getDayOfWeek().name().toLowerCase();
        String arrayServiceId = nextBusService.getArrayServiceId(dateWithoutDash, dayOfWeek);

        String startDateTime = date + " " + start;
        String endDateTime = date + " " + end;
        if (end.isBefore(start)) {
            LocalDate tomorrow = date.plusDays(1);
            endDateTime = tomorrow + " " + end;
        }

        BusSchedule busSchedule = new BusSchedule();
        List<StopSchedule> stopScheduleList = new ArrayList<>();
        List<Tuple> stopList = busScheduleRepository.getStop(routeShortName, directionId);
        for (Tuple tuple : stopList) {
            StopSchedule stopSchedule = new StopSchedule();
            stopSchedule.setStopCode(tuple.get("stop_code").toString());
            stopSchedule.setStopName(tuple.get("stop_name").toString());
            String stopId = tuple.get("stop_id").toString();
            List<Tuple> arrivalTimeList = busScheduleRepository.getArrivalTime(routeShortName, directionId, arrayServiceId, stopId, date.toString(), startDateTime, endDateTime);
            List<String> scheduleList = new ArrayList<>();
            for (Tuple arrivalTime : arrivalTimeList) {
                scheduleList.add(arrivalTime.get("time_schedule").toString());
            }
            stopSchedule.setArrivalTimes(scheduleList);
            stopScheduleList.add(stopSchedule);
        }
        busSchedule.setStopSchedules(stopScheduleList);
        return busSchedule;
    }
}
