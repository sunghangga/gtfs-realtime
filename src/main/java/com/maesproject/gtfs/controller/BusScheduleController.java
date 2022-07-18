package com.maesproject.gtfs.controller;

import com.maesproject.gtfs.entity.BusSchedule;
import com.maesproject.gtfs.service.api.BusScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class BusScheduleController {
    @Autowired
    private BusScheduleService busScheduleService;

    @GetMapping(value = "/api/gtfs/bus-schedules/find", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSuggestionRoute(@RequestParam String param) {
        String response = busScheduleService.getRouteByParam(param);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/gtfs/bus-schedules/route/{route}/direction/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSchedulesByRouteAndDirection(@PathVariable("route") String routeShortName,
                                                                  @PathVariable("direction") int directionId,
                                                                  @RequestParam(required = false) String dateCheck,
                                                                  @RequestParam(required = false) String startTime,
                                                                  @RequestParam(required = false) String endTime) {

        BusSchedule response = busScheduleService.getBusSchedule(routeShortName, directionId, dateCheck, startTime, endTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
