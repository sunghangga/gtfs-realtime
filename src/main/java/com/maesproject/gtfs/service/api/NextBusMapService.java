package com.maesproject.gtfs.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maesproject.gtfs.repository.NextBusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NextBusMapService {
    @Autowired
    private NextBusRepository nextBusRepository;
    @Autowired
    private NextBusService nextBusService;

    @Value("${timezone}")
    private String timeZone;

    public String getMapDepartureTimeByStop(String stopCode) {
        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        // get stop info
        ObjectNode objectStop = getObjectStopInfo(stopCode);
        objectNode.set("stop", objectStop);

        // get next departure time info
        ArrayNode arrayRoute = getArrayDepartureList(stopCode);
        objectNode.set("departures", arrayRoute);

        // get vehicle list
        ArrayNode arrayVehicle = getArrayVehicleList(stopCode, "");
        objectNode.set("vehicles", arrayVehicle);

        return objectNode.toString();
    }

    public String getMapDepartureTimeByStopAndRoute(String stopCode, String routeShortName) {
        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        // get stop info
        ObjectNode objectStop = getObjectStopInfo(stopCode);
        objectNode.set("stop", objectStop);

        // get next departure time info
        ArrayNode arrayRoute = getArrayDepartureList(stopCode);
        objectNode.set("departures", arrayRoute);

        // get vehicle list
        ArrayNode arrayVehicle = getArrayVehicleList(stopCode, routeShortName);
        objectNode.set("vehicles", arrayVehicle);

        // get route path list
        ArrayNode arrayRoutePathList = getArrayShapeList(stopCode, routeShortName);
        objectNode.set("routePaths", arrayRoutePathList);

        return objectNode.toString();
    }

    public String getMapVehicleAndRoutePath(String routeShortName) {
        // get vehicle list
        ArrayNode arrayVehicle = getArrayVehicleList("", routeShortName);

        // get vehicle path list
        ArrayNode arrayRoutePathList = getArrayShapeList("", routeShortName);

        // set object value
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.set("vehicles", arrayVehicle);
        objectNode.set("routePaths", arrayRoutePathList);

        return objectNode.toString();
    }

    public ObjectNode getObjectStopInfo(String stopCode) {
        ObjectNode objectStop = new ObjectMapper().createObjectNode();
        List<Tuple> stopInfo = nextBusRepository.getStopInfo(stopCode);
        for (Tuple tuple : stopInfo) {
            objectStop.put("stopName", tuple.get("stop_name").toString().replace("@", "at"));
            objectStop.put("stopCode", stopCode);
            objectStop.put("stopLatitude", tuple.get("stop_lat").toString());
            objectStop.put("stopLongitude", tuple.get("stop_lon").toString());
        }
        return objectStop;
    }

    public ArrayNode getArrayDepartureList(String stopCode) {
        ArrayNode arrayRoute = new ObjectMapper().createArrayNode();

        // find trip start date
        LocalDate tripStartDate = nextBusService.getTripStartDateByStop(stopCode);
        if (tripStartDate == null) return arrayRoute;
        String tripStartDateWithoutDash = tripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // get active service id
        String arrayServiceId = nextBusService.getActiveServiceId(tripStartDate);

        // set next trip start date and service id
        LocalDate nextTripStartDate = tripStartDate.plusDays(1);
        String nextTripStartDateWithoutDash = nextTripStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String arrayNextServiceId = nextBusService.getActiveServiceId(nextTripStartDate);

        String[] arrayTripStartDateUnion = {tripStartDateWithoutDash, nextTripStartDateWithoutDash};
        String[] arrayServiceIdUnion = {arrayServiceId, arrayNextServiceId};

        String routeShortNameCheck = "";
        List<Tuple> nextDepartureList = nextBusRepository.getMapDepartureTimeByStopUnion(stopCode, "", arrayServiceIdUnion, arrayTripStartDateUnion, timeZone);
        for (Tuple tupleRoute : nextDepartureList) {
            String route = tupleRoute.get("route_short_name").toString();
            if (routeShortNameCheck.equals(route)) continue;
            else routeShortNameCheck = route;

            ObjectNode objectRoute = new ObjectMapper().createObjectNode();
            objectRoute.put("routeShortName", routeShortNameCheck);

            String next = "";
            int nextCount = 0;
            for (Tuple tupleMinute : nextDepartureList) {
                if (!routeShortNameCheck.equals(tupleMinute.get("route_short_name").toString())) continue;

                String tripScheduleRelationship = tupleMinute.get("trip_schedule_relationship").toString();
                String stopScheduleRelationship = tupleMinute.get("stop_schedule_relationship").toString();
                String minute = tupleMinute.get("rounded_minute").toString();
                int minuteNumber = Integer.parseInt(minute);
                next = next.isEmpty() ? (minuteNumber > 2 ? minute : "Now") : (next + ", " + minute);
                if (tripScheduleRelationship.equals("CANCELED") || stopScheduleRelationship.equals("SKIPPED")) {
                    next += " C";
                }
                nextCount++;
                if (nextCount == 5) break;
            }
            objectRoute.put("departure", next + " min");

            arrayRoute.add(objectRoute);
        }
        return arrayRoute;
    }

    public ArrayNode getArrayVehicleList(String stopCode, String routeShortName) {
        ArrayNode arrayVehicle = new ObjectMapper().createArrayNode();
        List<Tuple> vehicleList = nextBusRepository.getMapVehicle(stopCode, routeShortName, timeZone);
        for (Tuple tupleVehicle : vehicleList) {
            LocalDateTime timestamp = LocalDateTime.parse(tupleVehicle.get("timestamp").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));

            ObjectNode objectVehicle = new ObjectMapper().createObjectNode();
            objectVehicle.put("routeShortName", tupleVehicle.get("route_short_name").toString());
            objectVehicle.put("tripHeadSign", tupleVehicle.get("trip_headsign").toString());
            objectVehicle.put("vehicleLabel", tupleVehicle.get("vehicle_label").toString());
            objectVehicle.put("positionLatitude", Double.parseDouble(tupleVehicle.get("position_latitude").toString()));
            objectVehicle.put("positionLongitude", Double.parseDouble(tupleVehicle.get("position_longitude").toString()));
            objectVehicle.put("timestamp", timestamp.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")).toLowerCase());

            arrayVehicle.add(objectVehicle);
        }
        return arrayVehicle;
    }

    public ArrayNode getArrayShapeList(String stopCode, String routeShortName) {
        ArrayNode arrayShape = new ObjectMapper().createArrayNode();
        List<Tuple> shapeList = nextBusRepository.getMapRoutePath(stopCode, routeShortName);
        String shapeId = "";
        for (Tuple tupleShapeId : shapeList) {
            if (shapeId.equals(tupleShapeId.get("shape_id").toString())) continue;

            shapeId = tupleShapeId.get("shape_id").toString();

            ObjectNode objectShape = new ObjectMapper().createObjectNode();
            objectShape.put("routeShortName", tupleShapeId.get("route_short_name").toString());
            objectShape.put("shapeId", shapeId);

            ArrayNode arrayShapeDetail = new ObjectMapper().createArrayNode();
            for (Tuple tupleShapeDetail : shapeList) {
                if (!shapeId.equals(tupleShapeDetail.get("shape_id").toString())) continue;

                ObjectNode objectShapeDetail = new ObjectMapper().createObjectNode();
                objectShapeDetail.put("shapePointSequence", tupleShapeDetail.get("shape_pt_sequence").toString());
                objectShapeDetail.put("shapePointLatitude", tupleShapeDetail.get("shape_pt_lat").toString());
                objectShapeDetail.put("shapePointLongitude", tupleShapeDetail.get("shape_pt_lon").toString());
                objectShapeDetail.put("shapeDistanceTraveled", tupleShapeDetail.get("shape_dist_traveled").toString());

                arrayShapeDetail.add(objectShapeDetail);
            }
            objectShape.set("coordinates", arrayShapeDetail);

            arrayShape.add(objectShape);
        }
        return arrayShape;
    }
}
