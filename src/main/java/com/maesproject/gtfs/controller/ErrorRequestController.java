package com.maesproject.gtfs.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ErrorRequestController implements ErrorController {

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> error(HttpServletResponse response) {
        Map<String, Object> object = new HashMap<>();
        object.put("message", HttpStatus.resolve(response.getStatus()));
        object.put("status", response.getStatus());
        return new ResponseEntity<>(object, HttpStatus.valueOf(response.getStatus()));
    }
}
