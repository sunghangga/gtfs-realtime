package com.maesproject.gtfs.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maesproject.gtfs.entity.BusSchedule;
import com.maesproject.gtfs.repository.BusScheduleRepository;
import com.maesproject.gtfs.repository.NextBusRepository;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private NextBusRepository nextBusRepository;
    @Autowired
    private NextBusService nextBusService;

    @Value("${timezone}")
    private String timeZone;

    public String getRouteByParam(String param) {
        ObjectNode objectResult = new ObjectMapper().createObjectNode();
        try {
            // find routes and directions
            ArrayNode arrayRoute = new ObjectMapper().createArrayNode();
            ObjectNode objectRoute = new ObjectMapper().createObjectNode();
            ArrayNode arrayDirection = new ObjectMapper().createArrayNode();

            LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
            String arrayServiceId = nextBusService.getActiveServiceId(tripStartDate);
            List<Tuple> routeDirectionList = busScheduleRepository.getRouteAndDirectionByParam(param, arrayServiceId);

            String lastRouteShortName = "";
            for (Tuple tuple : routeDirectionList) {
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

            // get trip head sign if direction not found
            if (arrayRoute.isEmpty()) {
                List<Tuple> tripHeadSignList = busScheduleRepository.getRouteAndTripHeadSignByParam(param, arrayServiceId);
                String routeCheck = "";
                for (Tuple tuple : tripHeadSignList) {
                    String routeShortName = tuple.get("route_short_name").toString();
                    if (routeCheck.equals(routeShortName)) continue;
                    else routeCheck = routeShortName;

                    objectRoute = new ObjectMapper().createObjectNode();
                    objectRoute.put("routeShortName", routeShortName);
                    objectRoute.put("routeLongName", tuple.get("route_long_name").toString());

                    arrayDirection = new ObjectMapper().createArrayNode();
                    String directionCheck = "";
                    for (Tuple tupleDirection : tripHeadSignList) {
                        if (!routeCheck.equals(tupleDirection.get("route_short_name").toString())) continue;

                        String directionId = tupleDirection.get("direction_id").toString();
                        if (directionCheck.equals(directionId)) continue;
                        else directionCheck = directionId;

                        ObjectNode objectDirection = new ObjectMapper().createObjectNode();
                        objectDirection.put("directionId", directionId);

                        String combinedTripHeadSign = "";
                        for (Tuple tupleTripHeadSign : tripHeadSignList) {
                            if (!routeShortName.equals(tupleTripHeadSign.get("route_short_name").toString())) continue;
                            if (!directionId.equals(tupleTripHeadSign.get("direction_id").toString())) continue;

                            String tripHeadSign = tupleTripHeadSign.get("trip_headsign").toString();
                            combinedTripHeadSign = combinedTripHeadSign.isEmpty() ? tripHeadSign : combinedTripHeadSign + " | " + tripHeadSign;
                        }
                        if (combinedTripHeadSign.isEmpty()) {
                            combinedTripHeadSign = directionId.equals("0") ? "Outbound" : "Inbound";
                        }

                        objectDirection.put("directionName", combinedTripHeadSign);
                        arrayDirection.add(objectDirection);
                    }
                    objectRoute.set("directions", arrayDirection);
                    arrayRoute.add(objectRoute);
                }
            }

            objectResult.set("routes", arrayRoute);

            // find lines
//            ArrayNode arrayLine = new ObjectMapper().createArrayNode();
//            objectResult.set("lines", arrayLine);

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return objectResult.toString();
    }

    public BusSchedule getBusSchedule(String routeShortName, int directionId, String dateCheck, String startTime, String endTime) {
        try {
            // define date check
            LocalDate date;
            if (dateCheck == null || dateCheck.isEmpty()) {
                date = LocalDate.now();
            } else {
                date = LocalDate.parse(dateCheck);
            }

            // define start time
            LocalTime start;
            if (startTime == null || startTime.isEmpty()) {
                startTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            start = LocalTime.parse(startTime);

            // define end time
            LocalTime end;
            if (endTime == null || endTime.isEmpty()) {
                endTime = "23:59";
            }
            end = LocalTime.parse(endTime);

            String startDateTime = date + " " + start;
            String endDateTime = date + " " + end;

            // check if date result include the next day
            LocalDate nextDate = date;
            if (end.isBefore(start)) {
                nextDate = date.plusDays(1);
                endDateTime = nextDate + " " + end;
            }

            // get service id
            String arrayServiceId = nextBusService.getActiveServiceId(date);

            // get stop
            List<BusSchedule.StopSchedule> stopScheduleList = new ArrayList<>();
            List<Tuple> stopList = busScheduleRepository.getStop(routeShortName, directionId);
            for (Tuple tuple : stopList) {
                String stopName = tuple.get("stop_name").toString()
                        .replace("Eastbound", "")
                        .replace("Westbound", "")
                        .replace("Northbound", "")
                        .replace("Southbound", "")
                        .trim();

                String stopCode = tuple.get("stop_code").toString();

                List<Tuple> arrivalTimeList = busScheduleRepository.getArrivalTime(routeShortName, directionId, arrayServiceId, stopCode, date.toString(), startDateTime, endDateTime);
                List<String> scheduleList = new ArrayList<>();
                for (Tuple arrivalTime : arrivalTimeList) {
                    LocalTime time = LocalTime.parse(arrivalTime.get("time_schedule").toString());
                    scheduleList.add(time.format(DateTimeFormatter.ofPattern("h:mm a")).toLowerCase());
                }

                BusSchedule.StopSchedule stopSchedule = new BusSchedule.StopSchedule();
                stopSchedule.setStopCode(stopCode);
                stopSchedule.setStopName(stopName);
                stopSchedule.setArrivalTimes(scheduleList);
                stopScheduleList.add(stopSchedule);
            }

            List<BusSchedule.RouteDirection> routeDirectionList = new ArrayList<>();

            // get direction
            List<Tuple> directionList = busScheduleRepository.getDirectionByRoute(routeShortName);
            for (Tuple tuple : directionList) {
                BusSchedule.RouteDirection routeDirection = new BusSchedule.RouteDirection();
                routeDirection.setDirectionId(Integer.parseInt(tuple.get("direction_id").toString()));
                routeDirection.setDirectionName(tuple.get("direction_name").toString());
                routeDirectionList.add(routeDirection);
            }

            // get trip head sign if direction not found
            if (routeDirectionList.isEmpty()) {
                List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRoute(routeShortName, arrayServiceId);
                String directionCheck = "";
                for (Tuple tuple : tripHeadSignList) {
                    String direction = tuple.get("direction_id").toString();
                    if (directionCheck.equals(direction)) continue;
                    else directionCheck = direction;

                    String combinedTripHeadSign = "";
                    for (Tuple tupleDirection : tripHeadSignList) {
                        if (!direction.equals(tupleDirection.get("direction_id").toString())) continue;

                        String tripHeadSign = tupleDirection.get("trip_headsign").toString();
                        combinedTripHeadSign = combinedTripHeadSign.isEmpty() ? tripHeadSign : combinedTripHeadSign + " | " + tripHeadSign;
                    }
                    if (combinedTripHeadSign.isEmpty()) {
                        combinedTripHeadSign = direction.equals("0") ? "Outbound" : "Inbound";
                    }

                    BusSchedule.RouteDirection routeDirection = new BusSchedule.RouteDirection();
                    routeDirection.setDirectionId(Integer.parseInt(direction));
                    routeDirection.setDirectionName(combinedTripHeadSign);
                    routeDirectionList.add(routeDirection);
                }
            }

            // get alert
            List<BusSchedule.AlertInfo> alertInfoList = new ArrayList<>();
            long seconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            List<Tuple> alertList = busScheduleRepository.getAlertByRoute(routeShortName, seconds);
            for (Tuple tuple : alertList) {
                String startDate = tuple.get("start_timestamp").toString();
                String endDate = tuple.get("end_timestamp") == null ? "unknown" : tuple.get("end_timestamp").toString();

                LocalDateTime startInfo = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
                startDate = startInfo.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy h:mm a"));

                if (!endDate.equals("unknown")) {
                    LocalDateTime endInfo = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
                    endDate = endInfo.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy h:mm a"));
                }

                BusSchedule.AlertInfo alertInfo = new BusSchedule.AlertInfo();
                alertInfo.setEffect(tuple.get("effect").toString());
                alertInfo.setDuration(startDate + " - " + endDate);
                alertInfo.setHeader(tuple.get("header_text").toString());
                alertInfoList.add(alertInfo);
            }

            String fromDate = date.format(DateTimeFormatter.ofPattern("EEE MMM dd"));
            String toDate = nextDate.format(DateTimeFormatter.ofPattern("EEE MMM dd"));

            BusSchedule busSchedule = new BusSchedule();
            busSchedule.setRouteShortName(routeShortName);
            busSchedule.setDirectionId(directionId);
            busSchedule.setDateCheck(date.toString());
            busSchedule.setStartDate(fromDate);
            busSchedule.setStartTime(startTime);
            busSchedule.setEndDate(toDate);
            busSchedule.setEndTime(endTime);
            busSchedule.setRouteDirections(routeDirectionList);
            busSchedule.setStopSchedules(stopScheduleList);
            busSchedule.setAlerts(alertInfoList);
            return busSchedule;

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return new BusSchedule();
    }
}
