package com.maesproject.gtfs.service;

import com.maesproject.gtfs.entity.nextbus.*;
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

        List<DestinationTrip> destinationTripList = new ArrayList<>();
        for (int i : directionList) {
            DestinationTrip destinationTrip = new DestinationTrip();
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
        List<StopCheck> stopCheckList = new ArrayList<>();
        for (Tuple tuple : stopList) {
            stopCheckList.add(new StopCheck(
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

        int oppositeDirection = (directionId == 0) ? 1 : 0;

        DestinationStop destinationStop = new DestinationStop();
        destinationStop.setRouteShortName(routeShortName);
        destinationStop.setCombinedTripHeadSign(headSign1 + " / " + headSign2);
        destinationStop.setOppositeDirection(oppositeDirection);
        destinationStop.setStopChecks(stopCheckList);
        return destinationStop;
    }

    public NextDeparture getNextDeparture(String routeShortName, String stopCode) {
        List<Tuple> lastDepartureList = nextBusRepository.getLastDeparture(routeShortName, stopCode);
        LocalTime lastDepartureTime = null;
        for (Tuple tuple : lastDepartureList) {
            lastDepartureTime = LocalTime.parse(tuple.get(0).toString());
        }

        LocalDate tripStartDate = null;
        if (LocalTime.now(ZoneId.of(timeZone)).isAfter(lastDepartureTime)) {
            tripStartDate = LocalDate.now(ZoneId.of(timeZone));
        } else {
            tripStartDate = LocalDate.now(ZoneId.of(timeZone)).minusDays(1);
        }

        String dateCheck = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = tripStartDate.getDayOfWeek().name().toLowerCase();
        String arrayServiceId = "";

        List<Tuple> serviceIdCalendar = nextBusRepository.getServiceIdCalendar(dateCheck, dayOfWeek);
        for (Tuple tuple : serviceIdCalendar) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get(0) + "'" : arrayServiceId + ", '" + tuple.get(0) + "'";
        }

        List<Tuple> serviceIdCalendarDate = nextBusRepository.getServiceIdCalendarDates(dateCheck);
        for (Tuple tuple : serviceIdCalendarDate) {
            arrayServiceId = arrayServiceId.isEmpty() ? "'" + tuple.get(0) + "'" : arrayServiceId + ", '" + tuple.get(0) + "'";
        }

        List<Tuple> tripHeadSignList = nextBusRepository.getTripHeadSignByStop(routeShortName, stopCode);
        List<DepartureTrip> departureTripList = new ArrayList<>();
        for (Tuple tuple : tripHeadSignList) {
            String tripHeadSign = tuple.get("trip_headsign").toString();
            List<Tuple> nextDepartureList = nextBusRepository.getNextDeparture(routeShortName, tripHeadSign, stopCode, arrayServiceId, dateCheck, timeZone);

            String nextInfo = "";
            for (Tuple tupleDeparture : nextDepartureList) {
                nextInfo += (nextInfo.isEmpty()) ? tupleDeparture.get(0) : ", " + tupleDeparture.get(0);
            }

            if (nextDepartureList.isEmpty()) {
                LocalDate tomorrow = LocalDate.now(ZoneId.of(timeZone)).plusDays(1);
                String date = tomorrow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String day = tomorrow.getDayOfWeek().name().toLowerCase();
                List<Tuple> nextScheduledList = nextBusRepository.getNextScheduled(routeShortName, tripHeadSign, stopCode, date, day);
                for (Tuple tupleScheduled : nextScheduledList) {
                    LocalTime nextScheduled = LocalTime.parse(tupleScheduled.get(0).toString());
                    nextInfo = "Scheduled at " + tomorrow.toString() + " " + nextScheduled.toString();
                }
            }

            departureTripList.add(new DepartureTrip(tripHeadSign, nextInfo));
        }

        NextDeparture nextDeparture = new NextDeparture();
        nextDeparture.setStopCode(stopCode);
        nextDeparture.setStopName("");
        nextDeparture.setRouteShortName(routeShortName);
        nextDeparture.setDirectionId(0);
        nextDeparture.setDepartureTrips(departureTripList);
        return nextDeparture;
    }
}
