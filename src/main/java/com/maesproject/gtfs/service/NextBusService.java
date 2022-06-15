package com.maesproject.gtfs.service;

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

    public Destination getDestination(String routeShortName) {
        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSign(routeShortName);
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

    public DestinationStop getStop(String routeShortName, int directionId) {
        List<Tuple> stopList = nextBusRepository.getStop(routeShortName, directionId);
        List<DestinationStop.StopCheck> stopCheckList = new ArrayList<>();
        for (Tuple tuple : stopList) {
            stopCheckList.add(new DestinationStop.StopCheck(
                    tuple.get("stop_code").toString(),
                    tuple.get("stop_name").toString().replace("@", "at")
            ));
        }

        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByDirection(routeShortName, directionId);
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
        // check if ongoing trip is from today's trip or from yesterday's trip
        // by comparing current time to the latest departure time of the route and stop
        List<Tuple> lastDepartureList = nextBusRepository.getLastDeparture(routeShortName, stopCode);
        LocalTime lastDepartureTime = null;
        for (Tuple tuple : lastDepartureList) {
            lastDepartureTime = LocalTime.parse(tuple.get("departure_time").toString());
        }
        if (lastDepartureTime == null) lastDepartureTime = LocalTime.now();

        LocalDate tripStartDate;
        if (LocalTime.now(ZoneId.of(timeZone)).isAfter(lastDepartureTime)) {
            // using today trip start date
            tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        } else {
            // using yesterday trip start date
            tripStartDate = LocalDate.now(ZoneId.of(timeZone)).minusDays(1);
        }

        String dateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = tripStartDate.getDayOfWeek().name().toLowerCase();
        String arrayServiceId = getArrayServiceId(dateWithoutDash, dayOfWeek);

        String stopName = "";
        int directionId = 9999;
        List<StopDeparture.DepartureSchedule> departureScheduleList = new ArrayList<>();

        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByStop(routeShortName, stopCode);
        for (Tuple tuple : tripHeadSignList) {
            if (stopName.isEmpty()) stopName = tuple.get("stop_name").toString().replace("@", "at");
            if (directionId == 9999) directionId = Integer.parseInt(tuple.get("direction_id").toString());

            String tripHeadSign = tuple.get("trip_headsign").toString();
            String departing = "";
            String nextSchedule = "";

            List<Tuple> nextDepartureList = nextBusRepository.getNextDeparture(routeShortName, tripHeadSign, stopCode, arrayServiceId, dateWithoutDash, timeZone);
            for (Tuple tupleDeparture : nextDepartureList) {
                if (departing.isEmpty()) {
                    double depart = Double.parseDouble(tupleDeparture.get("rounded_minute").toString());
                    if (depart <= 2) {
                        departing = "Now";
                    } else {
                        departing = tupleDeparture.get("rounded_minute").toString().replace(".0", "") + " Minutes";
                    }
                } else {
                    nextSchedule = (nextSchedule.isEmpty()) ? "" + tupleDeparture.get("rounded_minute") : nextSchedule + ", " + tupleDeparture.get("rounded_minute");
                }
            }

            if (nextDepartureList.isEmpty()) {
                nextSchedule = getNextScheduled(routeShortName, tripHeadSign, stopCode, arrayServiceId);
            } else {
                nextSchedule = nextSchedule.replace(".0", "") + " min";
            }

            departureScheduleList.add(new StopDeparture.DepartureSchedule(tripHeadSign, departing, nextSchedule));
        }

        StopDeparture stopDeparture = new StopDeparture();
        stopDeparture.setStopName(stopName);
        stopDeparture.setStopCode(stopCode);
        stopDeparture.setRouteShortName(routeShortName);
        stopDeparture.setDirectionId(directionId);
        stopDeparture.setDepartureSchedules(departureScheduleList);
        return stopDeparture;
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
            List<Tuple> nextScheduledList = nextBusRepository.getNextScheduled(routeShortName, tripHeadSign, stopCode, arrayServiceId, date, timeZone);
            if (nextScheduledList.isEmpty()) {
                add++;
                if (add >= 100) break;
                continue;
            }
            for (Tuple tupleScheduled : nextScheduledList) {
                LocalTime nextScheduled = LocalTime.parse(tupleScheduled.get("next_scheduled").toString());
                nextSchedule = "Scheduled at " + nextScheduled.format(DateTimeFormatter.ofPattern("H:ma")).toLowerCase();
                if (!nextTripStartDate.isEqual(LocalDate.now(ZoneId.of(timeZone)))) {
                    nextSchedule += " " + nextTripStartDate;
                }
            }
            break;
        }
        return nextSchedule;
    }
}
