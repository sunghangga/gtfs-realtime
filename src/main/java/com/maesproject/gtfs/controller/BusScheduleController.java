package com.maesproject.gtfs.controller;

import com.maesproject.gtfs.entity.busschedule.BusSchedule;
import com.maesproject.gtfs.service.api.BusScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class BusScheduleController {
    @Autowired
    private BusScheduleService busScheduleService;

    @GetMapping(value = "/api/gtfs/bus-schedules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDestination(@RequestParam String routeShortName,
                                                 @RequestParam int directionId,
                                                 @RequestParam String dateCheck,
                                                 @RequestParam String startTime,
                                                 @RequestParam String endTime) {

        BusSchedule response = busScheduleService.getBusSchedule(routeShortName, directionId, dateCheck, startTime, endTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
