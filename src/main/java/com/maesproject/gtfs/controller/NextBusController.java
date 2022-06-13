package com.maesproject.gtfs.controller;

import com.maesproject.gtfs.entity.nextbus.Destination;
import com.maesproject.gtfs.entity.nextbus.DestinationStop;
import com.maesproject.gtfs.entity.nextbus.StopDeparture;
import com.maesproject.gtfs.service.NextBusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin
@RestController
public class NextBusController {
    @Autowired
    private NextBusService nextBusService;

    @GetMapping(value = "/api/gtfs/nextbus")
    public RedirectView checkParam(@RequestParam String param) {
        String result = nextBusService.checkParam(param);
        if (result.equals("route")) {
            return new RedirectView("/api/gtfs/nextbus/route/" + param);
        } else if (result.equals("stop")) {
            return new RedirectView("/api/gtfs/nextbus/stop/" + param);
        } else {
            return new RedirectView("/api/gtfs/nextbus/invalid");
        }
    }

    @GetMapping(value = "/api/gtfs/nextbus/route/{route}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDestination(@PathVariable("route") String routeShortName) {
        Destination response = nextBusService.getDestination(routeShortName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/gtfs/nextbus/route/{route}/direction/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getStop(@PathVariable("route") String routeShortName,
                                          @PathVariable("direction") int directionId) {

        DestinationStop response = nextBusService.getStop(routeShortName, directionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/gtfs/nextbus/route/{route}/stop/{stop}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNextDeparture(@PathVariable("route") String routeShortName,
                                                   @PathVariable("stop") String stopCode) {

        StopDeparture response = nextBusService.getNextDeparture(routeShortName, stopCode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
