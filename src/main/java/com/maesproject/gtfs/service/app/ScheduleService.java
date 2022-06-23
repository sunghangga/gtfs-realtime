package com.maesproject.gtfs.service.app;

import com.maesproject.gtfs.repository.AlertRepository;
import com.maesproject.gtfs.repository.TripUpdateRepository;
import com.maesproject.gtfs.repository.VehiclePositionRepository;
import com.maesproject.gtfs.service.GtfsRealtimeConsumer;
import com.maesproject.gtfs.service.VehicleService;
import com.maesproject.gtfs.util.GlobalVariable;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduleService implements GlobalVariable {
    @Autowired
    private GtfsRealtimeConsumer gtfsRealtimeConsumer;
    @Autowired
    private TripUpdateRepository tripUpdateRepository;
    @Autowired
    private VehiclePositionRepository vehiclePositionRepository;
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private VehicleService vehicleService;

    @Value("${url.trip-update}")
    private String urlTripUpdate;
    @Value("${url.vehicle-position}")
    private String urlVehiclePosition;
    @Value("${url.alert}")
    private String urlAlert;
    @Value("${delete.realtime.minus-day}")
    private int minusDay;
    @Value("${delete.realtime.minus-second}")
    private int minusSecond;
    @Value("${print.count-info}")
    private boolean printCountInfo;
    @Value("${timezone}")
    private String timeZone;

    private long timestampTrip = 0;
    private long timestampVehicle = 0;
    private long timestampAlert = 0;
    private long timestampBusList = 0;

    private boolean printUrlInfoTripUpdate = true;
    private boolean printUrlInfoVehiclePosition = true;
    private boolean printUrlInfoAlert = true;

    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}", initialDelay = 200)
    public void consumeTripUpdate() {
        if (printUrlInfoTripUpdate) {
            Logger.info("Consuming data from " + urlTripUpdate);
            printUrlInfoTripUpdate = false;
        }
        timestampTrip = gtfsRealtimeConsumer.consume(urlTripUpdate, GTFS_TRIP_UPDATE, timestampTrip);
    }

    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}", initialDelay = 200)
    public void consumeVehiclePosition() {
        if (printUrlInfoVehiclePosition) {
            Logger.info("Consuming data from " + urlTripUpdate);
            printUrlInfoVehiclePosition = false;
        }
        timestampVehicle = gtfsRealtimeConsumer.consume(urlVehiclePosition, GTFS_VEHICLE_POSITION, timestampVehicle);
    }

    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}", initialDelay = 200)
    public void consumeAlert() {
        if (printUrlInfoAlert) {
            Logger.info("Consuming data from " + urlTripUpdate);
            printUrlInfoAlert = false;
        }
        timestampAlert = gtfsRealtimeConsumer.consume(urlAlert, GTFS_ALERT, timestampAlert);
    }

//    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}")
//    public void consumeVehicleId() {
//        timestampBusList = vehicleService.collectVehicleId(urlVehiclePosition, timestampBusList);
//    }

    @Scheduled(fixedDelayString = "${fixed.delay.delete.realtime.milliseconds}", initialDelay = 100)
    public void deleteExpiredRealtimeData() {
        try {
            // set timestamp parameter in seconds
            LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
            LocalDateTime dateTimeToDelete = now.minusDays(minusDay).minusSeconds(minusSecond);
            long timeInSeconds = dateTimeToDelete.atZone(ZoneId.of(timeZone)).toEpochSecond();
            String timeDelete = dateTimeToDelete.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            int resultTripUpdate = tripUpdateRepository.deleteByTimestampLessThan(timeInSeconds);
            int resultVehiclePosition = vehiclePositionRepository.deleteByTimestampLessThan(timeInSeconds);
            int resultAlert = alertRepository.deleteByEndLessThan(timeInSeconds);

            if (printCountInfo) {
                System.out.println();
                Logger.info("Deleting expired realtime data before " + timeDelete);
                Logger.info(resultTripUpdate + " trip update deleted");
                Logger.info(resultVehiclePosition + " vehicle position deleted");
                Logger.info(resultAlert + " alert deleted");
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }
}
