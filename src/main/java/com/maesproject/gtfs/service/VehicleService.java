package com.maesproject.gtfs.service;

import com.google.transit.realtime.GtfsRealtime;
import com.maesproject.gtfs.entity.Vehicle;
import com.maesproject.gtfs.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public long collectVehicleId(String feedUrl, long lastTimestamp) {
        GtfsRealtime.FeedMessage feed;

        try {
            URL url = new URL(feedUrl);
            feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
            if (feed.getEntityList().isEmpty()) return lastTimestamp;
        } catch (IOException e) {
            return lastTimestamp;
        }

        try {
            if (feed.getHeader().hasTimestamp()) {
                if (feed.getHeader().getTimestamp() == lastTimestamp) return lastTimestamp;
                else lastTimestamp = feed.getHeader().getTimestamp();
            }

            List<Vehicle> vehicleList = new ArrayList<>();
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    GtfsRealtime.VehiclePosition vehiclePosition = entity.getVehicle();
                    if (vehiclePosition.hasVehicle()) {
                        if (vehiclePosition.getVehicle().hasId()) {
                            vehicleList.add(new Vehicle(vehiclePosition.getVehicle().getId()));
                        } else if (vehiclePosition.getVehicle().hasLabel()) {
                            vehicleList.add(new Vehicle(vehiclePosition.getVehicle().getLabel()));
                        }
                    }
                }
            }

            vehicleRepository.saveAll(vehicleList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastTimestamp;
    }
}
