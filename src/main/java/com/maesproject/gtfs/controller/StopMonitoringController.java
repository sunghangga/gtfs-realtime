package com.maesproject.gtfs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maesproject.gtfs.service.api.StopMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@CrossOrigin
@RestController
public class StopMonitoringController {
    @Autowired
    private StopMonitoringService stopMonitoringService;

    @GetMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> checkApiReady() {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("api", "GTFS Realtime");
        response.put("version", "2.4.0");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // get stop monitoring real data
    @GetMapping(path = "api/gtfs/stopmonitoring", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> getStopMonitoring(@RequestParam(required = true) String agency_id,
                                                    @RequestParam(required = false) String stop_id,
                                                    @RequestParam(required = false) String vehicle_id,
                                                    @RequestParam(required = false, defaultValue = "json") String format,
                                                    @RequestParam(required = false, defaultValue = "0") Long approx) {

        HttpHeaders headers = new HttpHeaders();
        if (format != null && format.equalsIgnoreCase("xml")) {
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            headers.setContentType(MediaType.APPLICATION_XML);
            String response = stopMonitoringService.getStopMonitoringXml(agency_id, stop_id, vehicle_id, approx);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        } else {
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            String response = stopMonitoringService.getStopMonitoringJson(agency_id, stop_id, vehicle_id, approx);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }
    }

    // get stop monitoring dummy data
    @GetMapping(path = "api/gtfs/dummy/stopmonitoring", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> getDummyStopMonitoring(@RequestParam(required = true) String agency_id,
                                                         @RequestParam(required = false) String stop_id,
                                                         @RequestParam(required = false, defaultValue = "json") String format) {

        HttpHeaders headers = new HttpHeaders();
        if (format != null && format.equalsIgnoreCase("xml")) {
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            headers.setContentType(MediaType.APPLICATION_XML);
            String response = stopMonitoringService.getDummyStopMonitoringXml(agency_id, stop_id);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        } else {
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            String response = stopMonitoringService.getDummyStopMonitoringJson(agency_id, stop_id);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> missingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        String formatData = req.getParameter("format");
        String message = "Required request parameter '" + e.getParameterName() + "' for method parameter!";
        if (formatData != null && formatData.equals("xml")) {
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            headers.setContentType(MediaType.APPLICATION_XML);
            return new ResponseEntity<>(stopMonitoringService.errorMessageXml(message), headers, HttpStatus.BAD_REQUEST);
        } else {
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(stopMonitoringService.errorMessageJson(message), headers, HttpStatus.BAD_REQUEST);
        }
    }

}
