package com.maesproject.gtfs.service;

import com.maesproject.gtfs.entity.busschedule.BusSchedule;
import com.maesproject.gtfs.entity.busschedule.StopSchedule;
import com.maesproject.gtfs.repository.BusScheduleRepository;
import com.maesproject.gtfs.repository.NextBusRepository;
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
    private NextBusRepository nextBusRepository;

    public BusSchedule getBusSchedule(String routeShortName, int directionId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Tuple> stopList = busScheduleRepository.getStop(routeShortName, directionId);

        String dateCheck = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = date.getDayOfWeek().name().toLowerCase();
        String arrayServiceId = "";

        List<Tuple> serviceIdCalendar = nextBusRepository.getServiceIdCalendar(dateCheck, dayOfWeek);
        for (Tuple tuple : serviceIdCalendar) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get(0) + "'" : arrayServiceId + ", '" + tuple.get(0) + "'";
        }

        List<Tuple> serviceIdCalendarDate = nextBusRepository.getServiceIdCalendarDates(dateCheck);
        for (Tuple tuple : serviceIdCalendarDate) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get(0) + "'" : arrayServiceId + ", '" + tuple.get(0) + "'";
        }

        String startDateTime = date + " " + startTime.toString();
        String endDateTime = date + " " + endTime.toString();
        if (endTime.isBefore(startTime)) {
            LocalDate tomorrow = date.plusDays(1);
            endDateTime = tomorrow + " " + endTime;
        }

        BusSchedule busSchedule = new BusSchedule();
        List<StopSchedule> stopScheduleList = new ArrayList<>();
        for (Tuple tuple : stopList) {
            StopSchedule stopSchedule = new StopSchedule();
            stopSchedule.setStopCode(tuple.get(1).toString());
            stopSchedule.setStopName(tuple.get(2).toString());
            String stopId = tuple.get(0).toString();
            List<Tuple> arrivalTimeList = busScheduleRepository.getArrivalTime(routeShortName, directionId, arrayServiceId, stopId, date.toString(), startDateTime, endDateTime);
            List<String> scheduleList = new ArrayList<>();
            for (Tuple arrivalTime : arrivalTimeList) {
                scheduleList.add(arrivalTime.get(0).toString());
            }
            stopSchedule.setArrivalTimes(scheduleList);
            stopScheduleList.add(stopSchedule);
        }
        busSchedule.setStopSchedules(stopScheduleList);
        return busSchedule;
    }
}
