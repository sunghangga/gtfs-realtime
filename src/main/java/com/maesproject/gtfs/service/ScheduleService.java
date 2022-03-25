package com.maesproject.gtfs.service;

import com.maesproject.gtfs.repository.AlertRepository;
import com.maesproject.gtfs.repository.TripUpdateRepository;
import com.maesproject.gtfs.repository.VehiclePositionRepository;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduleService {
    @Autowired
    private GtfsRealtimeConsumer gtfsRealtimeConsumer;
    @Autowired
    private TripUpdateRepository tripUpdateRepository;
    @Autowired
    private VehiclePositionRepository vehiclePositionRepository;
    @Autowired
    private AlertRepository alertRepository;

    @Value("${url.trip-update}")
    private String urlTripUpdate;
    @Value("${url.vehicle-position}")
    private String urlVehiclePosition;
    @Value("${url.alert}")
    private String urlAlert;
    @Value("${delete.realtime.start-day}")
    private int startDay;
    @Value("${print.count-info}")
    private boolean printCountInfo;
    @Value("${timezone}")
    private String timeZone;

    @Async
    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void consumeTripUpdates() {
        gtfsRealtimeConsumer.consumeFeedOnce(urlTripUpdate);
    }

    @Async
    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void consumeVehiclePositions() {
        gtfsRealtimeConsumer.consumeFeedOnce(urlVehiclePosition);
    }

    @Async
    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void consumeAlerts() {
        gtfsRealtimeConsumer.consumeFeedOnce(urlAlert);
    }

    @Async
    @Scheduled(cron = "${cron.expression.delete-realtime}", zone = "${timezone}")
    public void deleteExpiredRealtimeData() {
        try {
            // set timestamp parameter in seconds
            LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
            LocalDateTime dayBefore = now.minusDays(startDay);
            long timeInSeconds = dayBefore.atZone(ZoneId.of(timeZone)).toEpochSecond();
            String timeDelete = dayBefore.atZone(ZoneId.of(timeZone)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

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
