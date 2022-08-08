package com.maesproject.gtfs.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.List;

@Service
public class NextBusService {
    @Autowired
    private NextBusRepository nextBusRepository;

    @Value("${timezone}")
    private String timeZone;

    public String getActiveRoutes() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        try {
            LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
            String arrayServiceId = getActiveServiceId(tripStartDate);
            List<Tuple> routeList = nextBusRepository.getActiveRoutes(arrayServiceId);
            for (Tuple tuple : routeList) {
                ObjectNode objectNode = new ObjectMapper().createObjectNode();
                objectNode.put("routeShortName", tuple.get("route_short_name").toString());
                objectNode.put("routeLongName", tuple.get("route_long_name").toString().replace("/", " / "));
                arrayNode.add(objectNode);
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return arrayNode.toString();
    }

    public String getRouteAndStopByParam(String param) {
        ArrayNode arrayRoute = new ObjectMapper().createArrayNode();
        ArrayNode arrayStop = new ObjectMapper().createArrayNode();
        try {
            // find routes
            LocalDate dateCheck = LocalDate.now(ZoneId.of(timeZone));
            String arrayServiceId = getActiveServiceId(dateCheck);
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

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        ObjectNode objectResult = new ObjectMapper().createObjectNode();
        objectResult.set("routes", arrayRoute);
        objectResult.set("stops", arrayStop);
        return objectResult.toString();
    }

    public String checkParam(String param) {
        try {
            // check if param matches with route short name
            int result = nextBusRepository.countRoute(param);
            if (result > 0) return "route";

            // check if param matches with stop code
            result = nextBusRepository.countStop(param);
            if (result > 0) return "stop";

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return "";
    }

    public String getDestinations(String routeShortName) {
        ArrayNode arrayDestination = new ObjectMapper().createArrayNode();
        String routeLongName = "";
        try {
            // get trip head sign (as direction)
            LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
            String arrayServiceId = getActiveServiceId(tripStartDate);

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
                    combinedTripHeadSign = combinedTripHeadSign.isEmpty() ? tripHeadSign : combinedTripHeadSign + " / " + tripHeadSign;
                }
                ObjectNode objectDestination = new ObjectMapper().createObjectNode();
                objectDestination.put("combinedTripHeadSign", combinedTripHeadSign);
                objectDestination.put("directionId", direction);
                arrayDestination.add(objectDestination);
            }

            List<Tuple> routeInfo = nextBusRepository.getRouteInfo(routeShortName);
            for (Tuple tuple : routeInfo) {
                routeLongName = tuple.get("route_long_name").toString();
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("routeShortName", routeShortName);
        objectNode.put("routeLongName", routeLongName);
        objectNode.set("destinationTrips", arrayDestination);
        return objectNode.toString();
    }

    public String getDestinationStops(String routeShortName, int directionId) {
        ArrayNode arrayStop = new ObjectMapper().createArrayNode();
        LocalDate tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        String arrayServiceId = getActiveServiceId(tripStartDate);

        String combinedTripHeadSign = "";
        try {
            // get stop
            List<Tuple> stopList = nextBusRepository.getStopByRouteAndDirection(routeShortName, directionId);
            for (Tuple tuple : stopList) {
                String stopCode = tuple.get("stop_code").toString();
                String stopName = tuple.get("stop_name").toString()
                        .replace("@", "at")
                        .replace("Eastbound", "")
                        .replace("Westbound", "")
                        .replace("Northbound", "")
                        .replace("Southbound", "")
                        .trim();

                ObjectNode objectStop = new ObjectMapper().createObjectNode();
                objectStop.put("stopCode", stopCode);
                objectStop.put("stopName", stopName);
                arrayStop.add(objectStop);
            }

            // get trip head sign
            List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRouteAndDirection(routeShortName, directionId, arrayServiceId);
            for (Tuple tuple : tripHeadSignList) {
                String tripHeadSign = tuple.get("trip_headsign").toString();
                combinedTripHeadSign = combinedTripHeadSign.isEmpty() ? tripHeadSign : combinedTripHeadSign + " / " + tripHeadSign;
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("routeShortName", routeShortName);
        objectNode.put("combinedTripHeadSign", combinedTripHeadSign);
        objectNode.put("oppositeDirection", directionId == 0 ? 1 : 0);
        objectNode.set("stopChecks", arrayStop);
        return objectNode.toString();
    }

    public String getNextDepartureByRouteAndStop(String routeShortName, String stopCode) {
        ArrayNode arrayDeparture = new ObjectMapper().createArrayNode();
        String stopName = "";
        String routeLongName = "";
        int directionId = 0;

        try {
            // find trip start date
            LocalDate tripStartDate = getTripStartDateByStop(stopCode);
            if (tripStartDate == null) {
                ObjectNode objectNode = new ObjectMapper().createObjectNode();
                objectNode.put("message", "No data available");
                return objectNode.toString();
            }

            String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // get active service id
            String arrayServiceId = getActiveServiceId(tripStartDate);

            // set trip start date and service id for the next day
            LocalDate nextTripStartDate = tripStartDate.plusDays(1);
            String nextTripStartDateWithoutDash = nextTripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nextArrayServiceId = getActiveServiceId(nextTripStartDate);

            String[] arrayTripStartDateUnion = {tripStartDateWithoutDash, nextTripStartDateWithoutDash};
            String[] arrayServiceIdUnion = {arrayServiceId, nextArrayServiceId};

            // get trip head sign
            List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByRouteAndStop(routeShortName, stopCode, arrayServiceId);
            for (Tuple tuple : tripHeadSignList) {
                String tripHeadSign = tuple.get("trip_headsign").toString();
                String departing = "";
                String next = "";
                String lastDepartureDateTime = "";

                // get next departure per trip head sign
                List<Tuple> nextDepartureList = nextBusRepository.getNextDeparturePerTripHeadSignUnion(routeShortName, stopCode, tripHeadSign, arrayServiceIdUnion, arrayTripStartDateUnion, timeZone);
                for (Tuple tupleDeparture : nextDepartureList) {
                    lastDepartureDateTime = tupleDeparture.get("departure_date_time").toString();
                    String tripScheduleRelationship = tupleDeparture.get("trip_schedule_relationship").toString();
                    String stopScheduleRelationship = tupleDeparture.get("stop_schedule_relationship").toString();
                    int minute = Integer.parseInt(tupleDeparture.get("rounded_minute").toString());

                    if (departing.isEmpty()) {
                        if (tripScheduleRelationship.equals("CANCELED") || stopScheduleRelationship.equals("SKIPPED")) {
                            departing = "The bus departing in " + minute + " minutes was canceled";
                        } else {
                            if (minute <= 2) {
                                departing = "Now";
                            } else {
                                departing = minute + " Minutes";
                            }
                        }
                    } else {
                        next = (next.isEmpty()) ? "" + tupleDeparture.get("rounded_minute") : next + ", " + tupleDeparture.get("rounded_minute");
                        if (tripScheduleRelationship.equals("CANCELED") || stopScheduleRelationship.equals("SKIPPED")) {
                            next += " C";
                        }
                    }
                }

                if (next.isEmpty()) {
                    // find next scheduled time
                    next = getNextScheduled(routeShortName, tripHeadSign, stopCode, lastDepartureDateTime);
                } else {
                    next = next + " min";
                }

                ObjectNode objectDeparture = new ObjectMapper().createObjectNode();
                objectDeparture.put("tripHeadSign", tripHeadSign);
                objectDeparture.put("departing", departing);
                objectDeparture.put("next", next);
                arrayDeparture.add(objectDeparture);
            }

            // get detail
            List<Tuple> stopInfo = nextBusRepository.getStopInfo(stopCode);
            for (Tuple tuple : stopInfo) {
                stopName = tuple.get("stop_name").toString().replace("@", "at");
            }

            List<Tuple> routeInfo = nextBusRepository.getRouteInfo(routeShortName);
            for (Tuple tuple : routeInfo) {
                routeLongName = tuple.get("route_long_name").toString();
            }

            List<Tuple> directionList = nextBusRepository.getDirectionByRouteAndStop(routeShortName, stopCode);
            for (Tuple tuple : directionList) {
                directionId = Integer.parseInt(tuple.get("direction_id").toString());
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("stopName", stopName);
        objectNode.put("stopCode", stopCode);
        objectNode.put("routeShortName", routeShortName);
        objectNode.put("routeLongName", routeLongName);
        objectNode.put("directionId", directionId);
        objectNode.set("departureSchedules", arrayDeparture);
        return objectNode.toString();
    }

    public String getNextDepartureByStop(String stopCode) {
        ArrayNode arrayRouteDeparture = new ObjectMapper().createArrayNode();
        String stopName = "";
        try {
            // find trip start date
            LocalDate tripStartDate = getTripStartDateByStop(stopCode);
            if (tripStartDate == null) {
                ObjectNode objectNode = new ObjectMapper().createObjectNode();
                objectNode.put("message", "No data available");
                return objectNode.toString();
            }

            String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // get service id
            String arrayServiceId = getActiveServiceId(tripStartDate);

            // set trip start date and service id for the next day
            LocalDate nextTripStartDate = tripStartDate.plusDays(1);
            String nextTripStartDateWithoutDash = nextTripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nextArrayServiceId = getActiveServiceId(nextTripStartDate);

            String[] arrayTripStartDateUnion = {tripStartDateWithoutDash, nextTripStartDateWithoutDash};
            String[] arrayServiceIdUnion = {arrayServiceId, nextArrayServiceId};

            // get route list
            List<Tuple> routeList = nextBusRepository.getRouteByStop(stopCode);
            for (Tuple tuple : routeList) {
                String routeShortName = tuple.get("route_short_name").toString();
                String routeLongName = tuple.get("route_long_name").toString();
                String departing = "";
                String nextInfo = "";
                String lastDepartureDateTime = "";

                List<Tuple> nextDepartureList = nextBusRepository.getNextDeparturePerRouteUnion(routeShortName, stopCode, arrayServiceIdUnion, arrayTripStartDateUnion, timeZone);
                for (Tuple tupleDeparture : nextDepartureList) {
                    lastDepartureDateTime = tupleDeparture.get("departure_date_time").toString();
                    String tripScheduleRelationship = tupleDeparture.get("trip_schedule_relationship").toString();
                    String stopScheduleRelationship = tupleDeparture.get("stop_schedule_relationship").toString();
                    int depart = Integer.parseInt(tupleDeparture.get("rounded_minute").toString());

                    if (departing.isEmpty()) {
                        if (tripScheduleRelationship.equals("CANCELED") || stopScheduleRelationship.equals("SKIPPED")) {
                            departing = "The bus departing in " + depart + " minutes was canceled";
                        } else {
                            if (depart <= 2) {
                                departing = "Now";
                            } else {
                                departing = depart + " Minutes";
                            }
                        }
                    } else {
                        nextInfo = (nextInfo.isEmpty()) ? "" + tupleDeparture.get("rounded_minute") : nextInfo + ", " + tupleDeparture.get("rounded_minute");
                        if (tripScheduleRelationship.equals("CANCELED") || stopScheduleRelationship.equals("SKIPPED")) {
                            nextInfo += " C";
                        }
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

            List<Tuple> stopInfo = nextBusRepository.getStopInfo(stopCode);
            for (Tuple tuple : stopInfo) {
                stopName = tuple.get("stop_name").toString().replace("@", "at");
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("stopName", stopName);
        objectNode.put("stopCode", stopCode);
        objectNode.set("departureSchedules", arrayRouteDeparture);
        return objectNode.toString();
    }

    public String getNextScheduled(String routeShortName, String tripHeadSign, String stopCode, String lastDepartureDateTime) {
        String nextSchedule = "";
        try {
            LocalDate tripStartDate = getTripStartDateByStop(stopCode);
            if (tripStartDate == null) return "";
            int add = 0;
            while (add < 2) {
                LocalDate nextTripStartDate = tripStartDate.plusDays(add);
                String dateCheck = nextTripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String arrayServiceId = getActiveServiceId(nextTripStartDate);

                LocalDateTime nextScheduled = nextBusRepository.getNextScheduledAfterLastTrip(routeShortName, tripHeadSign, stopCode, arrayServiceId, dateCheck, timeZone, lastDepartureDateTime);
                if (nextScheduled == null) {
                    add++;
                    continue;
                }
                LocalDate nextDate = nextScheduled.toLocalDate();
                nextSchedule = "Scheduled at " + nextScheduled.toLocalTime().format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase();
                if (!nextDate.isEqual(LocalDate.now(ZoneId.of(timeZone)))) {
                    nextSchedule += " " + nextDate;
                }
                break;
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return nextSchedule;
    }

    public LocalDate getTripStartDateByStop(String stopCode) {
        // check if ongoing trip is from today's trip or from yesterday's trip
        // by comparing current time to the latest departure time for that stop
        LocalDate tripStartDate = null;
        try {
            // get last departure time
            LocalTime lastDepartureTime = nextBusRepository.getLastDepartureTime(stopCode);
            if (lastDepartureTime == null) {
                Logger.error("Cannot find last departure time for stop '" + stopCode + "'! ");
                return null;
            }

            // define trip start date
            if (LocalTime.now(ZoneId.of(timeZone)).isAfter(lastDepartureTime)) {
                // using today trip start date
                tripStartDate = LocalDate.now(ZoneId.of(timeZone));
            } else {
                // using yesterday trip start date
                tripStartDate = LocalDate.now(ZoneId.of(timeZone)).minusDays(1);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return tripStartDate;
    }

    public String getActiveServiceId(LocalDate dateCheck) {
        String arrayServiceId = "";
        try {
            String dateWithoutDash = dateCheck.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dayOfWeek = dateCheck.getDayOfWeek().name().toLowerCase();
            List<Tuple> serviceIdCalendar = nextBusRepository.getAllActiveServiceId(dateWithoutDash, dayOfWeek);
            for (Tuple tuple : serviceIdCalendar) {
                arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get("service_id") + "'" : arrayServiceId + ", '" + tuple.get("service_id") + "'";
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return arrayServiceId;
    }

}
