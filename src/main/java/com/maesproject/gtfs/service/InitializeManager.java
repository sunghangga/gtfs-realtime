package com.maesproject.gtfs.service;

import com.google.transit.realtime.GtfsRealtime.*;
import com.maesproject.gtfs.entity.*;
import com.maesproject.gtfs.repository.*;
import com.maesproject.gtfs.util.DurationCounter;
import com.maesproject.gtfs.util.GlobalVariable;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InitializeManager implements GlobalVariable {
    @Autowired
    private TripUpdateRepository tripUpdateRepository;
    @Autowired
    private StopTimeUpdateRepository stopTimeUpdateRepository;
    @Autowired
    private VehiclePositionRepository vehiclePositionRepository;
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private EntitySelectorRepository entitySelectorRepository;

    @Value("${print.count-info}")
    private boolean printCountInfo;
    @Value("${timezone}")
    private String timeZone;
    @Value("${agency-filter}")
    private String agencyFilter;

    public void initializeData(FeedMessage feedMessage, String feedUrl, String type) {
        switch (type) {
            case GTFS_TRIP_UPDATE:
                saveTripUpdate(feedMessage);
                return;
            case GTFS_VEHICLE_POSITION:
                saveVehiclePosition(feedMessage);
                return;
            case GTFS_ALERT:
                saveAlert(feedMessage);
                return;
            default:
                Logger.warn("No action available for feed " + feedUrl);
        }
    }

    public boolean initializeData(FeedMessage feedMessage, String feedUrl) {
        if (feedUrl.toLowerCase().contains("trip")) {
            saveTripUpdate(feedMessage);
            return true;
        } else if (feedUrl.toLowerCase().contains("vehicle")) {
            saveVehiclePosition(feedMessage);
            return true;
        } else if (feedUrl.toLowerCase().contains("alert")) {
            saveAlert(feedMessage);
            return true;
        } else {
            Logger.warn("No action available for feed " + feedUrl);
            return false;
        }
    }

    public void saveTripUpdate(FeedMessage feed) {
        LocalDateTime start = LocalDateTime.now();
        List<StopTimeUpdateEntity> stopTimeUpdateEntities = new ArrayList<>();
        List<Long> tripUpdateIdList = new ArrayList<>();
        int tripUpdateSaved = 0;

        for (FeedEntity entity : feed.getEntityList()) {
            if (!agencyFilter.isEmpty()) {
                if (!entity.getId().toUpperCase().contains(agencyFilter)) continue;
            }

            if (entity.hasTripUpdate()) {
                TripUpdate tripUpdate = entity.getTripUpdate();

                // initial trip update
                TripUpdateEntity tripUpdateEntity = new TripUpdateEntity();
                if (tripUpdate.hasTrip()) {
                    if (tripUpdate.getTrip().hasTripId()) {
                        tripUpdateEntity.setTripId(tripUpdate.getTrip().getTripId());
                    }
                    if (tripUpdate.getTrip().hasRouteId()) {
                        tripUpdateEntity.setRouteId(tripUpdate.getTrip().getRouteId());
                    }
                    if (tripUpdate.getTrip().hasDirectionId()) {
                        tripUpdateEntity.setDirectionId(tripUpdate.getTrip().getDirectionId());
                    }
                    if (tripUpdate.getTrip().hasStartTime()) {
                        tripUpdateEntity.setTripStartTime(tripUpdate.getTrip().getStartTime());
                    }
                    if (tripUpdate.getTrip().hasStartDate()) {
                        tripUpdateEntity.setTripStartDate(tripUpdate.getTrip().getStartDate());
                    }
                    if (tripUpdate.getTrip().hasScheduleRelationship()) {
                        tripUpdateEntity.setScheduleRelationship(tripUpdate.getTrip().getScheduleRelationship().toString());
                    }
                }
                if (tripUpdate.hasVehicle()) {
                    if (tripUpdate.getVehicle().hasId()) {
                        tripUpdateEntity.setVehicleId(tripUpdate.getVehicle().getId());
                    }
                    if (tripUpdate.getVehicle().hasLabel()) {
                        tripUpdateEntity.setVehicleLabel(tripUpdate.getVehicle().getLabel());
                    }
                    if (tripUpdate.getVehicle().hasLicensePlate()) {
                        tripUpdateEntity.setVehicleLicensePlate(tripUpdate.getVehicle().getLicensePlate());
                    }
                }
                if (tripUpdate.hasTimestamp()) {
                    tripUpdateEntity.setTimestamp(tripUpdate.getTimestamp());
                } else {
                    LocalDateTime currentTimeNl = LocalDateTime.now(ZoneId.of(timeZone));
                    long seconds = currentTimeNl.atZone(ZoneId.of(timeZone)).toEpochSecond();
                    tripUpdateEntity.setTimestamp(seconds);
                }
                if (tripUpdate.hasDelay()) {
                    tripUpdateEntity.setDelay(tripUpdate.getDelay());
                }
                tripUpdateEntity.setEntityId(entity.getId());

                // find old data for this trip update
                List<TripUpdateEntity> oldTripUpdateList = tripUpdateRepository.findByTripId(tripUpdateEntity.getTripId());
                for (TripUpdateEntity tu : oldTripUpdateList) {
                    tripUpdateIdList.add(tu.getId());
                }

//                TripUpdateEntity oldTripUpdate = tripUpdateRepository.findByTripIdAndRouteIdAndDirectionIdAndTripStartDateAndTripStartTime(
//                        tripUpdateEntity.getTripId(),
//                        tripUpdateEntity.getRouteId(),
//                        tripUpdateEntity.getDirectionId(),
//                        tripUpdateEntity.getTripStartDate(),
//                        tripUpdateEntity.getTripStartTime()
//                );
//                if (oldTripUpdate != null) tripUpdateIdList.add(oldTripUpdate.getId());

                // save new trip update
                tripUpdateRepository.save(tripUpdateEntity);
                tripUpdateSaved++;

                // initial stop time update
                List<TripUpdate.StopTimeUpdate> stopTimeUpdateList = tripUpdate.getStopTimeUpdateList();
                for (TripUpdate.StopTimeUpdate stopTimeUpdate : stopTimeUpdateList) {
                    StopTimeUpdateEntity stopTimeUpdateEntity = new StopTimeUpdateEntity();
                    if (stopTimeUpdate.hasStopSequence()) {
                        stopTimeUpdateEntity.setStopSequence(stopTimeUpdate.getStopSequence());
                    }
                    if (stopTimeUpdate.hasStopId()) {
                        stopTimeUpdateEntity.setStopId(stopTimeUpdate.getStopId());
                    }
                    if (stopTimeUpdate.hasArrival()) {
                        if (stopTimeUpdate.getArrival().hasDelay()) {
                            stopTimeUpdateEntity.setArrivalDelay(stopTimeUpdate.getArrival().getDelay());
                        }
                        if (stopTimeUpdate.getArrival().hasTime()) {
                            stopTimeUpdateEntity.setArrivalTime(stopTimeUpdate.getArrival().getTime());
                        }
                        if (stopTimeUpdate.getArrival().hasUncertainty()) {
                            stopTimeUpdateEntity.setArrivalUncertainty(stopTimeUpdate.getArrival().getUncertainty());
                        }
                    }
                    if (stopTimeUpdate.hasDeparture()) {
                        if (stopTimeUpdate.getDeparture().hasDelay()) {
                            stopTimeUpdateEntity.setDepartureDelay(stopTimeUpdate.getDeparture().getDelay());
                        }
                        if (stopTimeUpdate.getDeparture().hasTime()) {
                            stopTimeUpdateEntity.setDepartureTime(stopTimeUpdate.getDeparture().getTime());
                        }
                        if (stopTimeUpdate.getDeparture().hasUncertainty()) {
                            stopTimeUpdateEntity.setDepartureUncertainty(stopTimeUpdate.getDeparture().getUncertainty());
                        }
                    }
                    if (stopTimeUpdate.hasScheduleRelationship()) {
                        stopTimeUpdateEntity.setScheduleRelationship(stopTimeUpdate.getScheduleRelationship().toString());
                    }
                    stopTimeUpdateEntity.setTripUpdateId(tripUpdateEntity.getId());

                    // collect new stop time update
                    stopTimeUpdateEntities.add(stopTimeUpdateEntity);
                }
            }
        }

        // save new stop time update
        int stopTimeUpdateSaved = stopTimeUpdateRepository.saveAll(stopTimeUpdateEntities).size();

        // delete old trip update (and old stop time update)
        int tripUpdateDeleted = tripUpdateRepository.deleteByIdIn(tripUpdateIdList);

        if (printCountInfo) {
            DurationCounter durationCounter = new DurationCounter(start, LocalDateTime.now(), false);
            System.out.println();
            Logger.info("trip update saved: " + tripUpdateSaved);
            Logger.info("trip update deleted: " + tripUpdateDeleted);
            Logger.info("stop time update saved: " + stopTimeUpdateSaved);
            Logger.info("time: " + durationCounter.getDuration());
        }
    }

    public void saveVehiclePosition(FeedMessage feed) {
        LocalDateTime start = LocalDateTime.now();
        List<VehiclePositionEntity> vehiclePositionEntities = new ArrayList<>();
        List<Long> vehiclePositionIdList = new ArrayList<>();

        for (FeedEntity entity : feed.getEntityList()) {
            if (!agencyFilter.isEmpty()) {
                if (!entity.getId().toUpperCase().contains(agencyFilter)) continue;
            }

            if (entity.hasVehicle()) {
                VehiclePosition vehiclePosition = entity.getVehicle();
                // initial vehicle position
                VehiclePositionEntity vehiclePositionEntity = new VehiclePositionEntity();
                if (vehiclePosition.hasTrip()) {
                    if (vehiclePosition.getTrip().hasTripId()) {
                        vehiclePositionEntity.setTripId(vehiclePosition.getTrip().getTripId());
                    }
                    if (vehiclePosition.getTrip().hasRouteId()) {
                        vehiclePositionEntity.setRouteId(vehiclePosition.getTrip().getRouteId());
                    }
                    if (vehiclePosition.getTrip().hasDirectionId()) {
                        vehiclePositionEntity.setDirectionId(vehiclePosition.getTrip().getDirectionId());
                    }
                    if (vehiclePosition.getTrip().hasStartTime()) {
                        vehiclePositionEntity.setTripStartTime(vehiclePosition.getTrip().getStartTime());
                    }
                    if (vehiclePosition.getTrip().hasStartDate()) {
                        vehiclePositionEntity.setTripStartDate(vehiclePosition.getTrip().getStartDate());
                    }
                    if (vehiclePosition.getTrip().hasScheduleRelationship()) {
                        vehiclePositionEntity.setScheduleRelationship(vehiclePosition.getTrip().getScheduleRelationship().toString());
                    }
                }
                if (vehiclePosition.hasVehicle()) {
                    if (vehiclePosition.getVehicle().hasId()) {
                        vehiclePositionEntity.setVehicleId(vehiclePosition.getVehicle().getId());
                    }
                    if (vehiclePosition.getVehicle().hasLabel()) {
                        vehiclePositionEntity.setVehicleLabel(vehiclePosition.getVehicle().getLabel());
                    }
                    if (vehiclePosition.getVehicle().hasLicensePlate()) {
                        vehiclePositionEntity.setVehicleLicensePlate(vehiclePosition.getVehicle().getLicensePlate());
                    }
                }
                if (vehiclePosition.hasPosition()) {
                    if (vehiclePosition.getPosition().hasLatitude()) {
                        vehiclePositionEntity.setPositionLatitude(vehiclePosition.getPosition().getLatitude());
                    }
                    if (vehiclePosition.getPosition().hasLongitude()) {
                        vehiclePositionEntity.setPositionLongitude(vehiclePosition.getPosition().getLongitude());
                    }
                    if (vehiclePosition.getPosition().hasBearing()) {
                        vehiclePositionEntity.setPositionBearing(vehiclePosition.getPosition().getBearing());
                    }
                    if (vehiclePosition.getPosition().hasOdometer()) {
                        vehiclePositionEntity.setPositionOdometer(vehiclePosition.getPosition().getOdometer());
                    }
                    if (vehiclePosition.getPosition().hasSpeed()) {
                        vehiclePositionEntity.setPositionSpeed(vehiclePosition.getPosition().getSpeed());
                    }
                }
                if (vehiclePosition.hasCurrentStopSequence()) {
                    vehiclePositionEntity.setCurrentStopSequence(vehiclePosition.getCurrentStopSequence());
                }
                if (vehiclePosition.hasStopId()) {
                    vehiclePositionEntity.setStopId(vehiclePosition.getStopId());
                }
                if (vehiclePosition.hasCurrentStatus()) {
                    vehiclePositionEntity.setCurrentStatus(vehiclePosition.getCurrentStatus().toString());
                }
                if (vehiclePosition.hasOccupancyStatus()) {
                    vehiclePositionEntity.setOccupancyStatus(vehiclePosition.getOccupancyStatus().toString());
                }
                if (vehiclePosition.hasCongestionLevel()) {
                    vehiclePositionEntity.setCongestionLevel(vehiclePosition.getCongestionLevel().toString());
                }
                if (vehiclePosition.hasTimestamp()) {
                    vehiclePositionEntity.setTimestamp(vehiclePosition.getTimestamp());
                }
                vehiclePositionEntity.setEntityId(entity.getId());

                // collect new vehicle position
                vehiclePositionEntities.add(vehiclePositionEntity);

                // find old data for this vehicle position
                List<VehiclePositionEntity> oldVehiclePositionList = vehiclePositionRepository.findByVehicleLabel(vehiclePositionEntity.getVehicleLabel());
                for (VehiclePositionEntity vp : oldVehiclePositionList) {
                    vehiclePositionIdList.add(vp.getId());
                }

//                VehiclePositionEntity oldVehiclePosition = vehiclePositionRepository.findByTripIdAndRouteIdAndDirectionIdAndTripStartDateAndTripStartTimeAndVehicleLabel(
//                        vehiclePositionEntity.getTripId(),
//                        vehiclePositionEntity.getRouteId(),
//                        vehiclePositionEntity.getDirectionId(),
//                        vehiclePositionEntity.getTripStartDate(),
//                        vehiclePositionEntity.getTripStartTime(),
//                        vehiclePositionEntity.getVehicleLabel()
//                );
//                if (oldVehiclePosition != null) vehiclePositionIdList.add(oldVehiclePosition.getId());
            }
        }

        // save new vehicle position
        int saved = vehiclePositionRepository.saveAll(vehiclePositionEntities).size();

        // delete old vehicle position
        int deleted = vehiclePositionRepository.deleteByIdIn(vehiclePositionIdList);

        if (printCountInfo) {
            DurationCounter durationCounter = new DurationCounter(start, LocalDateTime.now(), false);
            System.out.println();
            Logger.info("vehicle position saved: " + saved);
            Logger.info("vehicle position deleted: " + deleted);
            Logger.info("time: " + durationCounter.getDuration());
        }
    }

    public void saveAlert(FeedMessage feed) {
        LocalDateTime start = LocalDateTime.now();
        List<EntitySelectorEntity> selectorEntities = new ArrayList<>();
        List<Long> alertIdList = new ArrayList<>();
        int alertSaved = 0;

        for (FeedEntity entity : feed.getEntityList()) {
            if (!agencyFilter.isEmpty()) {
                if (!entity.getId().toUpperCase().contains(agencyFilter)) continue;
            }

            if (entity.hasAlert()) {
                Alert alert = entity.getAlert();

                // initial alerts
                List<TimeRange> timeRangeList = alert.getActivePeriodList();
                for (TimeRange timeRange : timeRangeList) {
                    AlertEntity alertEntity = new AlertEntity();
                    if (timeRange.hasStart()) {
                        alertEntity.setStart(timeRange.getStart());
                    }
                    if (timeRange.hasEnd()) {
                        alertEntity.setEnd(timeRange.getEnd());
                    }
                    if (alert.hasCause()) {
                        alertEntity.setCause(alert.getCause().toString());
                    }
                    if (alert.hasEffect()) {
                        alertEntity.setEffect(alert.getEffect().toString());
                    }
                    if (alert.hasUrl()) {
                        List<TranslatedString.Translation> urlList = alert.getUrl().getTranslationList();
                        if (!urlList.isEmpty()) {
                            alertEntity.setUrl(getTranslation("text", urlList));
                        }
                    }
                    if (alert.hasHeaderText()) {
                        List<TranslatedString.Translation> headerList = alert.getHeaderText().getTranslationList();
                        if (!headerList.isEmpty()) {
                            alertEntity.setHeaderText(getTranslation("text", headerList));
                        }
                    }
                    if (alert.hasDescriptionText()) {
                        List<TranslatedString.Translation> descriptionList = alert.getDescriptionText().getTranslationList();
                        if (!descriptionList.isEmpty()) {
                            alertEntity.setDescriptionText(getTranslation("text", descriptionList));
                        }
                    }
                    alertEntity.setEntityId(entity.getId());

                    // find old data for this alert
                    List<AlertEntity> oldAlertList = alertRepository.findByEntityId(alertEntity.getEntityId());
                    for (AlertEntity a : oldAlertList) {
                        alertIdList.add(a.getId());
                    }

//                    AlertEntity oldAlert = alertRepository.findByEntityId(alertEntity.getEntityId());
//                    if (oldAlert != null) alertIdList.add(oldAlert.getId());

                    // save new alert
                    alertRepository.save(alertEntity);
                    alertSaved++;

                    // initial entity selector
                    List<EntitySelector> entitySelectorList = alert.getInformedEntityList();
                    for (EntitySelector entitySelector : entitySelectorList) {
                        EntitySelectorEntity selectorEntity = new EntitySelectorEntity();
                        if (entitySelector.hasAgencyId()) {
                            selectorEntity.setAgencyId(entitySelector.getAgencyId());
                        }
                        if (entitySelector.hasRouteId()) {
                            selectorEntity.setRouteId(entitySelector.getRouteId());
                        }
                        if (entitySelector.hasRouteType()) {
                            selectorEntity.setRouteType(entitySelector.getRouteType());
                        }
                        if (entitySelector.hasStopId()) {
                            selectorEntity.setStopId(entitySelector.getStopId());
                        }
                        if (entitySelector.hasTrip()) {
                            if (entitySelector.getTrip().hasTripId()) {
                                selectorEntity.setTripId(entitySelector.getTrip().getTripId());
                            }
                            if (entitySelector.getTrip().hasRouteId()) {
                                selectorEntity.setTripRouteId(entitySelector.getTrip().getRouteId());
                            }
                            if (entitySelector.getTrip().hasDirectionId()) {
                                selectorEntity.setDirectionId(entitySelector.getTrip().getDirectionId());
                            }
                            if (entitySelector.getTrip().hasStartTime()) {
                                selectorEntity.setTripStartTime(entitySelector.getTrip().getStartTime());
                            }
                            if (entitySelector.getTrip().hasStartDate()) {
                                selectorEntity.setTripStartDate(entitySelector.getTrip().getStartDate());
                            }
                            if (entitySelector.getTrip().hasScheduleRelationship()) {
                                selectorEntity.setScheduleRelationship(entitySelector.getTrip().getScheduleRelationship().toString());
                            }
                        }
                        selectorEntity.setAlertId(alertEntity.getId());

                        // collect new entity selector
                        selectorEntities.add(selectorEntity);
                    }
                }
            }
        }

        // save new entity selector
        int entitySelectorSaved = entitySelectorRepository.saveAll(selectorEntities).size();

        // delete old alert (and old entity selector)
        int alertDeleted = alertRepository.deleteByIdIn(alertIdList);

        if (printCountInfo) {
            DurationCounter durationCounter = new DurationCounter(start, LocalDateTime.now(), false);
            System.out.println();
            Logger.info("alert saved: " + alertSaved);
            Logger.info("alert deleted: " + alertDeleted);
            Logger.info("entity selector saved: " + entitySelectorSaved);
            Logger.info("time: " + durationCounter.getDuration());
        }
    }

    public String getTranslation(String type, List<TranslatedString.Translation> translationList) {
        for (TranslatedString.Translation translation : translationList) {
            if (type.equals("text")) return translation.getText();
            if (type.equals("lang")) return translation.getLanguage();
        }
        return "";
    }
}
