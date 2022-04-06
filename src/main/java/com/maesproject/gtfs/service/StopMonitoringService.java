package com.maesproject.gtfs.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.maesproject.gtfs.entity.StopMonitoring;
import com.maesproject.gtfs.repository.StopMonitoringRepository;
import com.maesproject.gtfs.stopmonitor.*;
import com.maesproject.gtfs.util.GlobalVariable;
import com.maesproject.gtfs.util.Logger;
import com.maesproject.gtfs.util.TimeConverter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class StopMonitoringService implements GlobalVariable {
    @Autowired
    private StopMonitoringRepository stopMonitoringRepository;

    @Value("${timezone}")
    private String timeZone;

    private ObjectMapper mapper;

    public ObjectMapper initializeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Configuration Deserialization
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        objectMapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        // Configuration Serialization
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        objectMapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);

        // JAXB annotation
        objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(new AfterburnerModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new ISO8601DateFormat());

        return objectMapper;
    }

    public String getStopMonitoringJson(String agencyId, String stopId, String vehicleLabel) {
        List<StopMonitoring> resultList = stopMonitoringRepository.getStopMonitoring(agencyId, stopId, vehicleLabel);
        if (resultList.isEmpty()) {
            return emptyDataMessageJson();
        } else {
            return mapStopMonitoring(resultList, agencyId);
        }
    }

    public String getStopMonitoringXml(String agencyId, String stopId, String vehicleLabel) throws IOException {
        List<StopMonitoring> resultList = stopMonitoringRepository.getStopMonitoring(agencyId, stopId, vehicleLabel);
        if (resultList.isEmpty()) {
            return convertJsonToXml(emptyDataMessageJson(), "response");
        } else {
            String jsonData = mapStopMonitoring(resultList, agencyId);
            return convertJsonToXml(jsonData, "Gtfs");
        }
    }

    public String getDummyStopMonitoringJson(String agencyId, String stopId) throws IOException {
        List<StopMonitoring> resultList = getDummyStopMonitoring(agencyId, stopId);
        if (resultList.isEmpty()) {
            return emptyDataMessageJson();
        } else {
            return mapStopMonitoring(resultList, agencyId);
        }
    }

    public String getDummyStopMonitoringXml(String agencyId, String stopId) throws IOException {
        List<StopMonitoring> resultList = getDummyStopMonitoring(agencyId, stopId);
        if (resultList.isEmpty()) {
            return convertJsonToXml(emptyDataMessageJson(), "response");
        } else {
            String jsonData = mapStopMonitoring(resultList, agencyId);
            return convertJsonToXml(jsonData, "Gtfs");
        }
    }

    public List<StopMonitoring> getDummyStopMonitoring(String agencyId, String stopId) throws IOException {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new ClassPathResource(PATH_DUMMY_SM).getInputStream()));
        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

        List<StopMonitoring> stopMonitoringList = new ArrayList<>();
        Iterable<CSVRecord> csvRecords = csvParser.getRecords();
        for (CSVRecord csvRecord : csvRecords) {
            if (!csvRecord.get("agency_id").equals(agencyId)) continue;
            if (stopId != null && !stopId.isEmpty()) {
                if (!csvRecord.get("stop_id").equals(stopId)) continue;
            }
            StopMonitoring stopMonitoring = new StopMonitoring();
            stopMonitoring.setAgencyId(csvRecord.get("agency_id"));
            stopMonitoring.setRouteId(csvRecord.get("route_id"));
            stopMonitoring.setTripId(csvRecord.get("trip_id"));
            stopMonitoring.setDirectionId(Integer.parseInt(csvRecord.get("direction_id")));
            stopMonitoring.setStopId(csvRecord.get("stop_id"));
            stopMonitoring.setStopSequence(Integer.parseInt(csvRecord.get("stop_sequence")));
            stopMonitoring.setVehicleLabel(csvRecord.get("vehicle_label"));
            stopMonitoring.setTimestamp(Long.parseLong(csvRecord.get("timestamp")));
            stopMonitoring.setRouteLongName(csvRecord.get("route_long_name"));
            stopMonitoring.setOriginStopId(csvRecord.get("origin_stop_id"));
            stopMonitoring.setOriginStopName(csvRecord.get("origin_stop_name"));
            stopMonitoring.setDestinationStopId(csvRecord.get("destination_stop_id"));
            stopMonitoring.setDestinationStopName(csvRecord.get("destination_stop_name"));
            stopMonitoring.setCongestionLevel(csvRecord.get("congestion_level"));
            stopMonitoring.setPositionLongitude(Double.parseDouble(csvRecord.get("position_longitude")));
            stopMonitoring.setPositionLatitude(Double.parseDouble(csvRecord.get("position_latitude")));
            stopMonitoring.setPositionBearing(Double.parseDouble(csvRecord.get("position_bearing")));
            stopMonitoring.setOccupancyStatus(csvRecord.get("occupancy_status"));
            stopMonitoring.setStopName(csvRecord.get("stop_name"));
            stopMonitoring.setStopLon(csvRecord.get("stop_lon"));
            stopMonitoring.setStopLat(csvRecord.get("stop_lat"));
            stopMonitoring.setAimedArrivalTime(Duration.parse(csvRecord.get("aimed_arrival_time")));
            stopMonitoring.setExpectedArrivalTime(Long.parseLong(csvRecord.get("expected_arrival_time")));
            stopMonitoring.setAimedDepartureTime(Duration.parse(csvRecord.get("aimed_departure_time")));
            stopMonitoring.setExpectedDepartureTime(Long.parseLong(csvRecord.get("expected_departure_time")));
            stopMonitoring.setTripStartDate(csvRecord.get("trip_start_date"));
            stopMonitoring.setCurrentStatus(csvRecord.get("current_status"));
            stopMonitoringList.add(stopMonitoring);
        }
        return stopMonitoringList;
    }

    public String mapStopMonitoring(List<StopMonitoring> stopMonitoringList, String agencyId) {
        if (!stopMonitoringList.isEmpty()) {
            if (mapper == null) mapper = initializeObjectMapper();
            ObjectNode gtfs = mapper.createObjectNode();

            //-> ServiceDelivery
            ObjectNode serviceDelivery = mapper.createObjectNode();
            serviceDelivery.put("ResponseTimestamp", TimeConverter.currentZoneTime(timeZone));
            serviceDelivery.put("ProducerRef", agencyId);
            serviceDelivery.put("Status", true);

            //-> StopMonitoringDelivery
            ObjectNode stopMonitoringDelivery = mapper.createObjectNode();
            stopMonitoringDelivery.put("version", "1.4");
            stopMonitoringDelivery.put("ResponseTimestamp", TimeConverter.currentZoneTime(timeZone));
            stopMonitoringDelivery.put("Status", true);

            ArrayNode monitoredStopVisit = mapper.createArrayNode();
            ArrayNode monitoredStopVisitCancellation = mapper.createArrayNode();

            int i = 0;
            int size = stopMonitoringList.size();
            for (StopMonitoring stopMonitoring : stopMonitoringList) {
                if (stopMonitoring == null) {
                    Logger.error("Stop Monitoring result with null value found at index " + i + " from " + size + " data!");
                    continue;
                }
                i++;

                if (stopMonitoring.getTripScheduleRelationship() != null && stopMonitoring.getTripScheduleRelationship().equals("CANCELED")) {
                    //-> MonitoredStopVisitCancellation
                    ObjectNode monitoredStopVisitCancellationObj = mapper.createObjectNode();
                    monitoredStopVisitCancellationObj.put("RecordedAtTime", TimeConverter.unixToDateTime(timeZone, stopMonitoring.getTimestamp()));
                    monitoredStopVisitCancellationObj.put("MonitoringRef", stopMonitoring.getStopId());
                    monitoredStopVisitCancellationObj.put("RouteRef", stopMonitoring.getRouteId());
                    monitoredStopVisitCancellationObj.put("DirectionRef", DIRECTION[stopMonitoring.getDirectionId()]);

                    //-> FramedVehicleJourneyRef
                    ObjectNode framedVehicleJourneyRef2 = mapper.createObjectNode();
                    framedVehicleJourneyRef2.put("DataFrameRef", TimeConverter.convertTripDate(stopMonitoring.getTripStartDate()));
                    framedVehicleJourneyRef2.put("DatedVehicleJourneyRef", stopMonitoring.getTripId());
                    //-< FramedVehicleJourneyRef

                    monitoredStopVisitCancellationObj.set("FramedVehicleJourneyRef", framedVehicleJourneyRef2);
                    monitoredStopVisitCancellation.add(monitoredStopVisitCancellationObj);
                    //<- MonitoredStopVisitCancellation
                } else {
                    //-> MonitoredStopVisit
                    ObjectNode monitoredStopVisitObj = mapper.createObjectNode();
                    monitoredStopVisitObj.put("RecordedAtTime", TimeConverter.unixToDateTime(timeZone, stopMonitoring.getTimestamp()));
                    monitoredStopVisitObj.put("MonitoringRef", stopMonitoring.getStopId());

                    //-> MonitoredVehicleJourney
                    ObjectNode monitoredVehicleJourney = mapper.createObjectNode();
                    monitoredVehicleJourney.put("RouteRef", stopMonitoring.getRouteId());
                    monitoredVehicleJourney.put("DirectionRef", DIRECTION[stopMonitoring.getDirectionId()]);

                    //-> FramedVehicleJourneyRef
                    ObjectNode framedVehicleJourneyRef = mapper.createObjectNode();
                    framedVehicleJourneyRef.put("DataFrameRef", TimeConverter.convertTripDate(stopMonitoring.getTripStartDate()));
                    framedVehicleJourneyRef.put("DatedVehicleJourneyRef", stopMonitoring.getTripId());
                    //<- FramedVehicleJourneyRef

                    monitoredVehicleJourney.set("FramedVehicleJourneyRef", framedVehicleJourneyRef);

                    monitoredVehicleJourney.put("PublishedRouteName", stopMonitoring.getRouteLongName());
                    monitoredVehicleJourney.put("OperatorRef", stopMonitoring.getAgencyId());
                    monitoredVehicleJourney.put("OriginRef", stopMonitoring.getOriginStopId());
                    monitoredVehicleJourney.put("OriginName", stopMonitoring.getOriginStopName());
                    monitoredVehicleJourney.put("DestinationRef", stopMonitoring.getDestinationStopId());
                    monitoredVehicleJourney.put("DestinationName", stopMonitoring.getDestinationStopName());
                    monitoredVehicleJourney.put("Monitored", true);
                    monitoredVehicleJourney.put("InCongestion", stopMonitoring.getCongestionLevel());

                    //-> VehicleLocation
                    ObjectNode vehicleLocation = mapper.createObjectNode();
                    vehicleLocation.put("Longitude", stopMonitoring.getPositionLongitude());
                    vehicleLocation.put("Latitude", stopMonitoring.getPositionLatitude());
                    //<- VehicleLocation

                    monitoredVehicleJourney.set("VehicleLocation", vehicleLocation);

                    monitoredVehicleJourney.put("Bearing", stopMonitoring.getPositionBearing());
                    monitoredVehicleJourney.put("Occupancy", stopMonitoring.getOccupancyStatus());
                    monitoredVehicleJourney.put("VehicleRef", stopMonitoring.getVehicleLabel());

                    //-> MonitoredCall
                    ObjectNode monitoredCall = mapper.createObjectNode();
                    monitoredCall.put("StopScheduleRelationship", stopMonitoring.getStopScheduleRelationship());
                    monitoredCall.put("StopPointRef", stopMonitoring.getStopId());
                    monitoredCall.put("StopPointName", stopMonitoring.getStopName());
                    monitoredCall.put("VehicleLocationAtStop", "");
                    monitoredCall.put("VehicleAtStop", stopMonitoring.getCurrentStatus() == null ? null : stopMonitoring.getCurrentStatus().equals(STOPPED_AT));
                    monitoredCall.put("AimedArrivalTime", TimeConverter.durationToZoneTime(stopMonitoring.getAimedArrivalTime(), stopMonitoring.getTripStartDate()));
                    monitoredCall.put("ExpectedArrivalTime", TimeConverter.unixToDateTime(timeZone, stopMonitoring.getExpectedArrivalTime()));
                    monitoredCall.put("ArrivalDelay", stopMonitoring.getArrivalDelay());
                    monitoredCall.put("AimedDepartureTime", TimeConverter.durationToZoneTime(stopMonitoring.getAimedDepartureTime(), stopMonitoring.getTripStartDate()));
                    monitoredCall.put("ExpectedDepartureTime", TimeConverter.unixToDateTime(timeZone, stopMonitoring.getExpectedDepartureTime()));
                    monitoredCall.put("DepartureDelay", stopMonitoring.getDepartureDelay());
                    //monitoredCall.put("Distances", "");
                    //<- MonitoredCall

                    monitoredVehicleJourney.set("MonitoredCall", monitoredCall);
                    //<- MonitoredVehicleJourney

                    monitoredStopVisitObj.set("MonitoredVehicleJourney", monitoredVehicleJourney);
                    monitoredStopVisit.add(monitoredStopVisitObj);
                    //<- MonitoredStopVisit
                }
            }

            stopMonitoringDelivery.set("MonitoredStopVisit", monitoredStopVisit);
            if (monitoredStopVisitCancellation.size() > 0) {
                stopMonitoringDelivery.set("MonitoredStopVisitCancellation", monitoredStopVisitCancellation);
            }
            //<- StopMonitoringDelivery

            serviceDelivery.set("StopMonitoringDelivery", stopMonitoringDelivery);
            //<- ServiceDelivery

            gtfs.set("ServiceDelivery", serviceDelivery);
            return gtfs.toString();
        }
        return "";
    }

    public String emptyDataMessageJson() {
        if (mapper == null) mapper = initializeObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("message", "No data available");
        objectNode.put("status", "OK");
        return objectNode.toString();
    }

    public String convertJsonToXml(String jsonData, String rootName) throws JsonProcessingException {
        if (mapper == null) mapper = initializeObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = mapper.readTree(jsonData);
        return xmlMapper.writer().withRootName(rootName).writeValueAsString(jsonNode);
    }

    public void createDummyStopMonitoring(String agencyId, String stopId, String vehicleLabel) {
        List<StopMonitoring> resultList = stopMonitoringRepository.getStopMonitoring(agencyId, stopId, vehicleLabel);

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp").append(',');
        sb.append("stop_id").append(',');
        sb.append("route_id").append(',');
        sb.append("direction_id").append(',');
        sb.append("trip_start_date").append(',');
        sb.append("trip_id").append(',');
        sb.append("route_long_name").append(',');
        sb.append("agency_id").append(',');
        sb.append("origin_stop_id").append(',');
        sb.append("origin_stop_name").append(',');
        sb.append("destination_stop_id").append(',');
        sb.append("destination_stop_name").append(',');
        sb.append("congestion_level").append(',');
        sb.append("position_longitude").append(',');
        sb.append("position_latitude").append(',');
        sb.append("position_bearing").append(',');
        sb.append("occupancy_status").append(',');
        sb.append("vehicle_label").append(',');
        sb.append("stop_name").append(',');
        sb.append("stop_lon").append(',');
        sb.append("stop_lat").append(',');
        sb.append("current_status").append(',');
        sb.append("aimed_arrival_time").append(',');
        sb.append("expected_arrival_time").append(',');
        sb.append("aimed_departure_time").append(',');
        sb.append("expected_departure_time").append(',');
        sb.append("stop_sequence").append('\n');

        try (PrintWriter writer = new PrintWriter("src/main/resources/static/data/dummy_stop_monitoring.csv")) {
            for (StopMonitoring stopMonitoring : resultList) {
                sb.append(stopMonitoring.getTimestamp()).append(',');
                sb.append(stopMonitoring.getStopId()).append(',');
                sb.append(stopMonitoring.getRouteId()).append(',');
                sb.append(stopMonitoring.getDirectionId()).append(',');
                sb.append(stopMonitoring.getTripStartDate()).append(',');
                sb.append(stopMonitoring.getTripId()).append(',');
                sb.append('"').append(stopMonitoring.getRouteLongName()).append('"').append(',');
                sb.append(stopMonitoring.getAgencyId()).append(',');
                sb.append(stopMonitoring.getOriginStopId()).append(',');
                sb.append('"').append(stopMonitoring.getOriginStopName()).append('"').append(',');
                sb.append(stopMonitoring.getDestinationStopId()).append(',');
                sb.append('"').append(stopMonitoring.getDestinationStopName()).append('"').append(',');
                sb.append(stopMonitoring.getCongestionLevel()).append(',');
                sb.append(stopMonitoring.getPositionLongitude()).append(',');
                sb.append(stopMonitoring.getPositionLatitude()).append(',');
                sb.append(stopMonitoring.getPositionBearing()).append(',');
                sb.append(stopMonitoring.getOccupancyStatus()).append(',');
                sb.append(stopMonitoring.getVehicleLabel()).append(',');
                sb.append('"').append(stopMonitoring.getStopName()).append('"').append(',');
                sb.append(stopMonitoring.getStopLon()).append(',');
                sb.append(stopMonitoring.getStopLat()).append(',');
                sb.append(stopMonitoring.getCurrentStatus()).append(',');
                sb.append(stopMonitoring.getAimedArrivalTime()).append(',');
                sb.append(stopMonitoring.getExpectedArrivalTime()).append(',');
                sb.append(stopMonitoring.getAimedDepartureTime()).append(',');
                sb.append(stopMonitoring.getExpectedDepartureTime()).append(',');
                sb.append(stopMonitoring.getStopSequence()).append('\n');
            }
            writer.write(sb.toString());
            Logger.info("Dummy stop monitoring data successfully created!");

        } catch (FileNotFoundException e) {
            Logger.error(e.getMessage());
        }
    }
}
