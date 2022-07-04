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
import java.util.HashMap;
import java.util.Map;

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
    @Value("${delete.realtime.minus.day-time}")
    private String minusDayTime;
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
    private boolean insertTripInProgress = false;
    private boolean insertVehicleInProgress = false;
    private boolean insertAlertInProgress = false;

    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}", initialDelay = 500)
    public void consumeTripUpdate() {
        if (printUrlInfoTripUpdate) {
            Logger.info("Consuming data from " + urlTripUpdate);
            printUrlInfoTripUpdate = false;
        }
        insertTripInProgress = true;
        timestampTrip = gtfsRealtimeConsumer.consume(urlTripUpdate, GTFS_TRIP_UPDATE, timestampTrip);
        insertTripInProgress = false;
    }

    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}", initialDelay = 500)
    public void consumeVehiclePosition() {
        if (printUrlInfoVehiclePosition) {
            Logger.info("Consuming data from " + urlVehiclePosition);
            printUrlInfoVehiclePosition = false;
        }
        insertVehicleInProgress = true;
        timestampVehicle = gtfsRealtimeConsumer.consume(urlVehiclePosition, GTFS_VEHICLE_POSITION, timestampVehicle);
        insertVehicleInProgress = false;
    }

    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}", initialDelay = 500)
    public void consumeAlert() {
        if (printUrlInfoAlert) {
            Logger.info("Consuming data from " + urlAlert);
            printUrlInfoAlert = false;
        }
        insertAlertInProgress = true;
        timestampAlert = gtfsRealtimeConsumer.consume(urlAlert, GTFS_ALERT, timestampAlert);
        insertAlertInProgress = false;
    }

//    @Scheduled(fixedDelayString = "${fixed.delay.consume.realtime.milliseconds}")
//    public void consumeVehicleId() {
//        timestampBusList = vehicleService.collectVehicleId(urlVehiclePosition, timestampBusList);
//    }

    @Scheduled(fixedDelayString = "${fixed.delay.delete.realtime.milliseconds}", initialDelay = 100)
    public void deleteExpiredRealtimeData() {
        try {
            Map<String, String> mapResult = new HashMap<>();

            // check if data is being inserted
            while (insertTripInProgress) {
                Thread.sleep(1000);
            }
            deleteExpiredData(GTFS_TRIP_UPDATE, mapResult);

            while (insertVehicleInProgress) {
                Thread.sleep(1000);
            }
            deleteExpiredData(GTFS_VEHICLE_POSITION, mapResult);

            while (insertAlertInProgress) {
                Thread.sleep(1000);
            }
            deleteExpiredData(GTFS_ALERT, mapResult);

            if (printCountInfo) {
                System.out.println();
                Logger.info("Deleting " + mapResult.get("tripUpdateDeleteCount") + " expired trip update data before " + mapResult.get("tripUpdateTimeDelete"));
                Logger.info("Deleting " + mapResult.get("vehiclePositionDeleteCount") + " expired vehicle position data before " + mapResult.get("vehiclePositionTimeDelete"));
                Logger.info("Deleting " + mapResult.get("alertDeleteCount") + " expired alert data before " + mapResult.get("alertTimeDelete"));
            }

        } catch (Exception e) {
            Logger.error("Cannot delete old realtime data! " + e.getMessage());
        }
    }

    public void deleteExpiredData(String dataType, Map<String, String> mapResult) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
        String[] arrayMinusDayTime = minusDayTime.split("-");
        LocalDateTime dateTimeToDelete = now
                .minusDays(Integer.parseInt(arrayMinusDayTime[0]))
                .minusHours(Integer.parseInt(arrayMinusDayTime[1]))
                .minusMinutes(Integer.parseInt(arrayMinusDayTime[2]))
                .minusSeconds(Integer.parseInt(arrayMinusDayTime[3]));

        long timeInSeconds = dateTimeToDelete.atZone(ZoneId.of(timeZone)).toEpochSecond();
        String timeDelete = dateTimeToDelete.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (dataType.equals(GTFS_TRIP_UPDATE)) {
            int resultTripUpdate = tripUpdateRepository.deleteByTimestampLessThan(timeInSeconds);
            mapResult.put("tripUpdateTimeDelete", timeDelete);
            mapResult.put("tripUpdateDeleteCount", "" + resultTripUpdate);
        }

        if (dataType.equals(GTFS_VEHICLE_POSITION)) {
            int resultVehiclePosition = vehiclePositionRepository.deleteByTimestampLessThan(timeInSeconds);
            mapResult.put("vehiclePositionTimeDelete", timeDelete);
            mapResult.put("vehiclePositionDeleteCount", "" + resultVehiclePosition);
        }

        if (dataType.equals(GTFS_ALERT)) {
            int resultAlert = alertRepository.deleteByEndLessThan(timeInSeconds);
            mapResult.put("alertTimeDelete", timeDelete);
            mapResult.put("alertDeleteCount", "" + resultAlert);
        }
    }

}
