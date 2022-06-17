package com.maesproject.gtfs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maesproject.gtfs.entity.nextbus.Destination;
import com.maesproject.gtfs.entity.nextbus.DestinationStop;
import com.maesproject.gtfs.entity.nextbus.StopDeparture;
import com.maesproject.gtfs.repository.NextBusRepository;
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
public class NextBusService {
    @Autowired
    private NextBusRepository nextBusRepository;

    @Value("${timezone}")
    private String timeZone;

    public String checkParam(String param) {
        int result = nextBusRepository.countRoute(param);
        if (result > 0) return "route";
        result = nextBusRepository.countStop(param);
        if (result > 0) return "stop";
        return "";
    }

    public Destination getDestinations(String routeShortName) {
        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRoute(routeShortName);
        String routeLongName = "";
        List<Integer> directionList = new ArrayList<>();
        for (Tuple tuple : tripHeadSignList) {
            if (routeLongName.isEmpty()) {
                routeLongName = tuple.get("route_long_name").toString();
            }
            int direction = Integer.parseInt(tuple.get("direction_id").toString());
            if (!directionList.contains(direction)) {
                directionList.add(direction);
            }
        }

        List<Destination.DestinationTrip> destinationTripList = new ArrayList<>();
        for (int i : directionList) {
            Destination.DestinationTrip destinationTrip = new Destination.DestinationTrip();
            String headSign1 = "";
            String headSign2 = "";
            for (Tuple tuple : tripHeadSignList) {
                int direction = Integer.parseInt(tuple.get("direction_id").toString());
                if (direction == i) {
                    String tripHeadSign = tuple.get("trip_headsign").toString();
                    if (tripHeadSign.toLowerCase().contains("to ")) {
                        headSign1 = tripHeadSign;
                    } else {
                        headSign2 = tripHeadSign;
                    }
                }
            }
            destinationTrip.setCombinedTripHeadSign(headSign1 + " / " + headSign2);
            destinationTrip.setDirectionId(i);
            destinationTripList.add(destinationTrip);
        }

        Destination destination = new Destination();
        destination.setRouteShortName(routeShortName);
        destination.setRouteLongName(routeLongName);
        destination.setDestinationTrips(destinationTripList);
        return destination;
    }

    public DestinationStop getDestinationStops(String routeShortName, int directionId) {
        List<Tuple> stopList = nextBusRepository.getStop(routeShortName, directionId);
        List<DestinationStop.StopCheck> stopCheckList = new ArrayList<>();
        for (Tuple tuple : stopList) {
            stopCheckList.add(new DestinationStop.StopCheck(
                    tuple.get("stop_code").toString(),
                    tuple.get("stop_name").toString().replace("@", "at")
            ));
        }

        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRouteDirection(routeShortName, directionId);
        String headSign1 = "";
        String headSign2 = "";
        for (Tuple tuple : tripHeadSignList) {
            String tripHeadSign = tuple.get("trip_headsign").toString();
            if (tripHeadSign.toLowerCase().contains("to ")) {
                headSign1 = tripHeadSign;
            } else {
                headSign2 = tripHeadSign;
            }
        }

        DestinationStop destinationStop = new DestinationStop();
        destinationStop.setRouteShortName(routeShortName);
        destinationStop.setCombinedTripHeadSign(headSign1 + " / " + headSign2);
        destinationStop.setOppositeDirection((directionId == 0) ? 1 : 0);
        destinationStop.setStopChecks(stopCheckList);
        return destinationStop;
    }

    public StopDeparture getNextDeparture(String routeShortName, String stopCode) {
        // get trip start date
        LocalDate tripStartDate = getTripStartDate(stopCode);
        if (tripStartDate == null) return new StopDeparture();

        // get service id
        String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = tripStartDate.getDayOfWeek().name().toLowerCase();
        String arrayServiceId = getArrayServiceId(tripStartDateWithoutDash, dayOfWeek);

        String stopName = "";
        String routeLongName = "";
        int directionId = 9999;
        List<StopDeparture.DepartureSchedule> departureScheduleList = new ArrayList<>();

        // get trip head sign
        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRouteAndStop(routeShortName, stopCode);
        for (Tuple tuple : tripHeadSignList) {
            // initial stop detail
            if (stopName.isEmpty()) stopName = tuple.get("stop_name").toString().replace("@", "at");
            if (routeLongName.isEmpty()) routeLongName = tuple.get("route_long_name").toString();
            if (directionId == 9999) directionId = Integer.parseInt(tuple.get("direction_id").toString());

            String tripHeadSign = tuple.get("trip_headsign").toString();
            String departing = "";
            String nextInfo = "";

            // get next departure
            List<Tuple> nextDepartureList = nextBusRepository.getNextDepartureByTripHeadSign(routeShortName, tripHeadSign, stopCode, arrayServiceId, tripStartDateWithoutDash, timeZone);
            for (Tuple tupleDeparture : nextDepartureList) {
                if (departing.isEmpty()) {
                    double depart = Double.parseDouble(tupleDeparture.get("rounded_minute").toString());
                    if (depart <= 2) {
                        departing = "Now";
                    } else {
                        departing = tupleDeparture.get("rounded_minute").toString() + " Minutes";
                    }
                } else {
                    nextInfo = (nextInfo.isEmpty()) ? "" + tupleDeparture.get("rounded_minute") : nextInfo + ", " + tupleDeparture.get("rounded_minute");
                }
            }

            if (nextInfo.isEmpty()) {
                // find next scheduled time
                nextInfo = getNextScheduled(routeShortName, tripHeadSign, stopCode, arrayServiceId);
            } else {
                nextInfo += " min";
            }

            departureScheduleList.add(new StopDeparture.DepartureSchedule(tripHeadSign, departing, nextInfo));
        }

        StopDeparture stopDeparture = new StopDeparture();
        stopDeparture.setStopName(stopName);
        stopDeparture.setStopCode(stopCode);
        stopDeparture.setRouteShortName(routeShortName);
        stopDeparture.setRouteLongName(routeLongName);
        stopDeparture.setDirectionId(directionId);
        stopDeparture.setDepartureSchedules(departureScheduleList);
        return stopDeparture;
    }

    public String getNextDepartureByStop(String stopCode) {
        // get trip start date
        LocalDate tripStartDate = getTripStartDate(stopCode);
        if (tripStartDate == null) {
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("message", "No data available");
            return objectNode.toString();
        }

        // get service id
        String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = tripStartDate.getDayOfWeek().name().toLowerCase();
        String arrayServiceId = getArrayServiceId(tripStartDateWithoutDash, dayOfWeek);

        String stopName = "";
        ArrayNode arrayRouteDeparture = new ObjectMapper().createArrayNode();

        // get route list
        List<Tuple> routeList = nextBusRepository.getRouteByStop(stopCode);
        for (Tuple tupleRoute : routeList) {
            // initial stop detail
            if (stopName.isEmpty()) stopName = tupleRoute.get("stop_name").toString().replace("@", "at");

            String routeShortName = tupleRoute.get("route_short_name").toString();
            String routeLongName = tupleRoute.get("route_long_name").toString();
            String departing = "";
            String nextInfo = "";

            List<Tuple> nextDepartureList = nextBusRepository.getNextDepartureByRoute(routeShortName, stopCode, arrayServiceId, tripStartDateWithoutDash, timeZone);
            for (Tuple tuple : nextDepartureList) {
                if (departing.isEmpty()) {
                    int depart = Integer.parseInt(tuple.get("rounded_minute").toString());
                    if (depart <= 2) {
                        departing = "Now";
                    } else {
                        departing = tuple.get("rounded_minute").toString() + " Minutes";
                    }
                } else {
                    nextInfo = (nextInfo.isEmpty()) ? "" + tuple.get("rounded_minute") : nextInfo + ", " + tuple.get("rounded_minute");
                }
            }

            if (nextInfo.isEmpty()) {
                // find next scheduled time
                nextInfo = getNextScheduled("", "", stopCode, arrayServiceId);
            } else {
                nextInfo += " min";
            }

            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("routeShortName", routeShortName);
            objectNode.put("routeLongName", routeLongName);
            objectNode.put("departing", departing);
            objectNode.put("next", nextInfo);
            arrayRouteDeparture.add(objectNode);
        }

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("stopName", stopName);
        objectNode.put("stopCode", stopCode);
        objectNode.set("departureSchedules", arrayRouteDeparture);

        return objectNode.toString();
    }

    public LocalDate getTripStartDate(String stopCode) {
        // check if ongoing trip is from today's trip or from yesterday's trip
        // by comparing current time to the latest departure time of the route/stop

        // get last departure time
        LocalTime lastDepartureTime = nextBusRepository.getLastDepartureTime(stopCode);
        if (lastDepartureTime == null) return null;

        // define trip start date
        LocalDate tripStartDate;
        if (LocalTime.now(ZoneId.of(timeZone)).isAfter(lastDepartureTime)) {
            // using today trip start date
            tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        } else {
            // using yesterday trip start date
            tripStartDate = LocalDate.now(ZoneId.of(timeZone)).minusDays(1);
        }
        return tripStartDate;
    }

    public String getArrayServiceId(String dateWithoutDash, String dayOfWeek) {
        String arrayServiceId = "";
        List<Tuple> serviceIdCalendar = nextBusRepository.getServiceIdCalendar(dateWithoutDash, dayOfWeek);
        for (Tuple tuple : serviceIdCalendar) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get("service_id") + "'" : arrayServiceId + ", '" + tuple.get("service_id") + "'";
        }
        List<Tuple> serviceIdCalendarDate = nextBusRepository.getServiceIdCalendarDates(dateWithoutDash);
        for (Tuple tuple : serviceIdCalendarDate) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get("service_id") + "'" : arrayServiceId + ", '" + tuple.get("service_id") + "'";
        }
        return arrayServiceId;
    }

    public String getNextScheduled(String routeShortName, String tripHeadSign, String stopCode, String arrayServiceId) {
        int add = 0;
        String nextSchedule = "";
        while (true) {
            LocalDate nextTripStartDate = LocalDate.now(ZoneId.of(timeZone)).plusDays(add);
            String date = nextTripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nextScheduled = nextBusRepository.getNextScheduled(routeShortName, tripHeadSign, stopCode, arrayServiceId, date, timeZone);
            if (nextScheduled == null) {
                add++;
                if (add >= 100) break;
                continue;
            }
            nextSchedule = "Scheduled at " + nextScheduled;
            if (!nextTripStartDate.isEqual(LocalDate.now(ZoneId.of(timeZone)))) {
                nextSchedule += " " + nextTripStartDate;
            }
            break;
        }
        return nextSchedule;
    }
}
