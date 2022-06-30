package com.maesproject.gtfs.service.api;

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
import java.time.LocalDateTime;
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

    public String getRouteAndStopByParam(String param) {
        ObjectNode objectResult = new ObjectMapper().createObjectNode();
        ArrayNode arrayRoute = new ObjectMapper().createArrayNode();
        ArrayNode arrayStop = new ObjectMapper().createArrayNode();

        // find routes
        LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        String arrayServiceId = getAllActiveServiceId(tripStartDate);
        List<Tuple> routeList = nextBusRepository.getRouteByParam(param, arrayServiceId);
        for (Tuple tuple : routeList) {
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("routeShortName", tuple.get("route_short_name").toString());
            objectNode.put("routeLongName", tuple.get("route_long_name").toString());
            arrayRoute.add(objectNode);
        }

        // find stops
        List<Tuple> stopList = nextBusRepository.getStopByParam(param);
        for (Tuple tuple : stopList) {
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("stopCode", tuple.get("stop_code").toString());
            objectNode.put("StopName", tuple.get("stop_name").toString());
            arrayStop.add(objectNode);
        }

        objectResult.set("routes", arrayRoute);
        objectResult.set("stops", arrayStop);
        return objectResult.toString();
    }

    public String checkParam(String param) {
        int result = nextBusRepository.countRoute(param);
        if (result > 0) return "route";
        result = nextBusRepository.countStop(param);
        if (result > 0) return "stop";
        return "";
    }

    public String getAllRoutes() {
        LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        String arrayServiceId = getAllActiveServiceId(tripStartDate);
        List<Tuple> routeList = nextBusRepository.getAllRoutes(arrayServiceId);
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        for (Tuple tuple : routeList) {
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("routeShortName", tuple.get("route_short_name").toString());
            objectNode.put("routeLongName", tuple.get("route_long_name").toString().replace("/", " / "));
            arrayNode.add(objectNode);
        }
        return arrayNode.toString();
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
            String combinedTripHeadSign = "";
            for (Tuple tuple : tripHeadSignList) {
                int direction = Integer.parseInt(tuple.get("direction_id").toString());
                if (direction == i) {
                    String tripHeadSign = tuple.get("trip_headsign").toString();
                    combinedTripHeadSign = combinedTripHeadSign.isEmpty() ? tripHeadSign : combinedTripHeadSign + " / " + tripHeadSign;
                }
            }
            destinationTrip.setCombinedTripHeadSign(combinedTripHeadSign);
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
        List<Tuple> stopList = nextBusRepository.getStopByRouteAndDirection(routeShortName, directionId);
        List<DestinationStop.StopCheck> stopCheckList = new ArrayList<>();
        for (Tuple tuple : stopList) {
            stopCheckList.add(new DestinationStop.StopCheck(
                    tuple.get("stop_code").toString(),
                    tuple.get("stop_name").toString()
                            .replace("@", "at")
                            .replace("Eastbound", "")
                            .replace("Westbound", "")
                            .replace("Northbound", "")
                            .replace("Southbound", "")
                            .trim()
            ));
        }

        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRouteDirection(routeShortName, directionId);
        String combinedTripHeadSign = "";
        for (Tuple tuple : tripHeadSignList) {
            String tripHeadSign = tuple.get("trip_headsign").toString();
            combinedTripHeadSign = combinedTripHeadSign.isEmpty() ? tripHeadSign : combinedTripHeadSign + " / " + tripHeadSign;
        }

        DestinationStop destinationStop = new DestinationStop();
        destinationStop.setRouteShortName(routeShortName);
        destinationStop.setCombinedTripHeadSign(combinedTripHeadSign);
        destinationStop.setOppositeDirection((directionId == 0) ? 1 : 0);
        destinationStop.setStopChecks(stopCheckList);
        return destinationStop;
    }

    public StopDeparture getNextDepartureByRouteAndStop(String routeShortName, String stopCode) {
        // get trip start date
        LocalDate tripStartDate = getTripStartDate(stopCode);
        if (tripStartDate == null) return new StopDeparture();

        String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // get service id
        String arrayServiceId = getAllActiveServiceId(tripStartDate);

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
            String lastDepartureDateTime = "";

            // get next departure
            List<Tuple> nextDepartureList = nextBusRepository.getNextDeparturePerTripHeadSignWithDelay(routeShortName, stopCode, tripHeadSign, arrayServiceId, tripStartDateWithoutDash, timeZone);
            for (Tuple tupleDeparture : nextDepartureList) {
                lastDepartureDateTime = tupleDeparture.get("departure_date_time").toString();
                if (departing.isEmpty()) {
                    int depart = Integer.parseInt(tupleDeparture.get("rounded_minute_with_delay").toString().replace(".0", ""));
                    if (depart <= 2) {
                        departing = "Now";
                    } else {
                        departing = depart + " Minutes";
                    }
                } else {
                    nextInfo = (nextInfo.isEmpty()) ? "" + tupleDeparture.get("rounded_minute_with_delay") : nextInfo + ", " + tupleDeparture.get("rounded_minute_with_delay");
                }
            }

            if (nextInfo.isEmpty()) {
                // find next scheduled time
                nextInfo = getNextScheduled(routeShortName, tripHeadSign, stopCode, lastDepartureDateTime);
            } else {
                nextInfo = nextInfo.replace(".0", "") + " min";
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

        String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // get service id
        String arrayServiceId = getAllActiveServiceId(tripStartDate);

        String stopName = "";
        ArrayNode arrayRouteDeparture = new ObjectMapper().createArrayNode();

        // get route list
        List<Tuple> routeList = nextBusRepository.getRouteByStop(stopCode);
        for (Tuple tuple : routeList) {
            // initial stop detail
            if (stopName.isEmpty()) stopName = tuple.get("stop_name").toString().replace("@", "at");

            String routeShortName = tuple.get("route_short_name").toString();
            String routeLongName = tuple.get("route_long_name").toString();
            String departing = "";
            String nextInfo = "";
            String lastDepartureDateTime = "";

            List<Tuple> nextDepartureList = nextBusRepository.getNextDeparturePerRouteWithDelay(routeShortName, stopCode, arrayServiceId, tripStartDateWithoutDash, timeZone);
            for (Tuple tupleDeparture : nextDepartureList) {
                lastDepartureDateTime = tupleDeparture.get("departure_date_time").toString();
                if (departing.isEmpty()) {
                    int depart = Integer.parseInt(tupleDeparture.get("rounded_minute_with_delay").toString());
                    if (depart <= 2) {
                        departing = "Now";
                    } else {
                        departing = depart + " Minutes";
                    }
                } else {
                    nextInfo = (nextInfo.isEmpty()) ? "" + tupleDeparture.get("rounded_minute_with_delay") : nextInfo + ", " + tupleDeparture.get("rounded_minute_with_delay");
                }
            }

            if (nextInfo.isEmpty()) {
                // find next scheduled time
                nextInfo = getNextScheduled(routeShortName, "", stopCode, lastDepartureDateTime);
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

    public String getRealtimeTripStartDate() {
        return "";
    }

    public String getTripStartDateWithoutDash(String stopCode) {
        LocalDate tripStartDate = getTripStartDate(stopCode);
        return tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
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

    public String getAllActiveServiceId(LocalDate dateCheck) {
        String arrayServiceId = "";
        String dateWithoutDash = dateCheck.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = dateCheck.getDayOfWeek().name().toLowerCase();
        List<Tuple> serviceIdCalendar = nextBusRepository.getAllActiveServiceId(dateWithoutDash, dayOfWeek);
        for (Tuple tuple : serviceIdCalendar) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get("service_id") + "'" : arrayServiceId + ", '" + tuple.get("service_id") + "'";
        }
        return arrayServiceId;
    }

    public String getNextScheduled(String routeShortName, String tripHeadSign, String stopCode, String lastDepartureDateTime) {
        int add = 0;
        String nextSchedule = "";
        LocalDate tripStartDate = getTripStartDate(stopCode);
        while (true) {
            LocalDate nextTripStartDate = tripStartDate.plusDays(add);
            String date = nextTripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String arrayServiceId = getAllActiveServiceId(nextTripStartDate);

            LocalDateTime nextScheduled = nextBusRepository.getNextScheduledAfterLastTrip(routeShortName, tripHeadSign, stopCode, arrayServiceId, date, timeZone, lastDepartureDateTime);
            if (nextScheduled == null) {
                add++;
                if (add >= 30) break;
                continue;
            }
            LocalDate nextDate = nextScheduled.toLocalDate();
            nextSchedule = "Scheduled at " + nextScheduled.toLocalTime().format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase();
            if (!nextDate.isEqual(LocalDate.now(ZoneId.of(timeZone)))) {
                nextSchedule += " " + nextDate;
            }
            break;
        }
        return nextSchedule;
    }
}
