package com.maesproject.gtfs.service;

import com.maesproject.gtfs.repository.AlertRepository;
import com.maesproject.gtfs.repository.TripUpdateRepository;
import com.maesproject.gtfs.repository.VehiclePositionRepository;
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
    private TripUpdateRepository tripUpdateRepository;
    @Autowired
    private VehiclePositionRepository vehiclePositionRepository;
    @Autowired
    private AlertRepository alertRepository;
    @Value("${delete.realtime.days.interval}")
    private int numberOfDays;
    @Value("${print.count-info}")
    private boolean printCountInfo;

    @Scheduled(cron = "${cron.expression.delete}", zone = TIME_ZONE_NL)
    public void deleteExpiredRealtimeData() {
        try {
            // set timestamp parameter in seconds
            LocalDateTime now = LocalDateTime.now(ZoneId.of(TIME_ZONE_NL));
            LocalDateTime dayBefore = now.minusDays(numberOfDays);
            long timeInSeconds = dayBefore.atZone(ZoneId.of(TIME_ZONE_NL)).toEpochSecond();
            String timeDelete = dayBefore.atZone(ZoneId.of(TIME_ZONE_NL)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

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
