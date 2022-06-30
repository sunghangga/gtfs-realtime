package com.maesproject.gtfs.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maesproject.gtfs.entity.busschedule.BusSchedule;
import com.maesproject.gtfs.repository.BusScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusScheduleService {
    @Autowired
    private BusScheduleRepository busScheduleRepository;
    @Autowired
    private NextBusService nextBusService;

    @Value("${timezone}")
    private String timeZone;

    public String getRouteByParam(String param) {
        ArrayNode arrayRoute = new ObjectMapper().createArrayNode();
        ArrayNode arrayDirection = new ObjectMapper().createArrayNode();
        ObjectNode objectRoute = new ObjectMapper().createObjectNode();

        LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        String arrayServiceId = nextBusService.getAllActiveServiceId(tripStartDate);
        List<Tuple> routeList = busScheduleRepository.getRouteAndDirectionByParam(param, arrayServiceId);

        String lastRouteShortName = "";
        for (Tuple tuple : routeList) {
            String routeShortName = tuple.get("route_short_name").toString();
            if (routeShortName.equals(lastRouteShortName)) {
                ObjectNode objectDirection = new ObjectMapper().createObjectNode();
                objectDirection.put("directionId", tuple.get("direction_id").toString());
                objectDirection.put("directionName", tuple.get("direction_name").toString());
                arrayDirection.add(objectDirection);
            } else {
                lastRouteShortName = routeShortName;
                objectRoute = new ObjectMapper().createObjectNode();
                objectRoute.put("routeShortName", tuple.get("route_short_name").toString());
                objectRoute.put("routeLongName", tuple.get("route_long_name").toString());
                arrayRoute.add(objectRoute);
                ObjectNode objectDirection = new ObjectMapper().createObjectNode();
                objectDirection.put("directionId", tuple.get("direction_id").toString());
                objectDirection.put("directionName", tuple.get("direction_name").toString());
                arrayDirection = new ObjectMapper().createArrayNode();
                arrayDirection.add(objectDirection);
                continue;
            }
            objectRoute.set("directions", arrayDirection);
        }
        return arrayRoute.toString();
    }

    public BusSchedule getBusSchedule(String routeShortName, int directionId, String dateCheck, String startTime, String endTime) {
        LocalDate date;
        if (dateCheck == null || dateCheck.isEmpty()) {
            date = LocalDate.now(ZoneId.of(timeZone));
        } else {
            date = LocalDate.parse(dateCheck);
        }

        LocalTime start;
        if (startTime == null || startTime.isEmpty()) {
            startTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        start = LocalTime.parse(startTime);

        LocalTime end;
        if (endTime == null || endTime.isEmpty()) {
            endTime = "23:59";
        }
        end = LocalTime.parse(endTime);

        String arrayServiceId = nextBusService.getAllActiveServiceId(date);

        String startDateTime = date + " " + start;
        String endDateTime = date + " " + end;
        if (end.isBefore(start)) {
            LocalDate tomorrow = date.plusDays(1);
            endDateTime = tomorrow + " " + end;
        }

        List<BusSchedule.StopSchedule> stopScheduleList = new ArrayList<>();
        List<Tuple> stopList = busScheduleRepository.getStop(routeShortName, directionId);
        for (Tuple tuple : stopList) {
            String stopName = tuple.get("stop_name").toString()
                    .replace("Eastbound", "")
                    .replace("Westbound", "")
                    .replace("Northbound", "")
                    .replace("Southbound", "")
                    .trim();
            BusSchedule.StopSchedule stopSchedule = new BusSchedule.StopSchedule();
            stopSchedule.setStopCode(tuple.get("stop_code").toString());
            stopSchedule.setStopName(stopName);
            String stopId = tuple.get("stop_id").toString();
            List<Tuple> arrivalTimeList = busScheduleRepository.getArrivalTime(routeShortName, directionId, arrayServiceId, stopId, date.toString(), startDateTime, endDateTime);
            List<String> scheduleList = new ArrayList<>();
            for (Tuple arrivalTime : arrivalTimeList) {
                LocalTime time = LocalTime.parse(arrivalTime.get("time_schedule").toString());
                scheduleList.add(time.format(DateTimeFormatter.ofPattern("h:mm a")).toLowerCase());
            }
            stopSchedule.setArrivalTimes(scheduleList);
            stopScheduleList.add(stopSchedule);
        }

        List<BusSchedule.RouteDirection> routeDirectionList = new ArrayList<>();
        List<Tuple> directionList = busScheduleRepository.getDirectionByRoute(routeShortName);
        for (Tuple tuple : directionList) {
            BusSchedule.RouteDirection routeDirection = new BusSchedule.RouteDirection();
            routeDirection.setDirectionId(Integer.parseInt(tuple.get("direction_id").toString()));
            routeDirection.setDirectionName(tuple.get("direction_name").toString());
            routeDirectionList.add(routeDirection);
        }

        BusSchedule busSchedule = new BusSchedule();
        busSchedule.setRouteShortName(routeShortName);
        busSchedule.setDirectionId(directionId);
        busSchedule.setDateCheck(date.toString());
        busSchedule.setStartTime(startTime);
        busSchedule.setEndTime(endTime);
        busSchedule.setRouteDirections(routeDirectionList);
        busSchedule.setStopSchedules(stopScheduleList);
        return busSchedule;
    }
}
