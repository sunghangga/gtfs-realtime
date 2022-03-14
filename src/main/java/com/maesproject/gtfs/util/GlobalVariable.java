package com.maesproject.gtfs.util;

public interface GlobalVariable {
    // feed url
    String URL_TRIP_UPDATE = "http://gtfs.ovapi.nl/nl/tripUpdates.pb";
    String URL_VEHICLE_POSITION = "http://gtfs.ovapi.nl/nl/vehiclePositions.pb";
    String URL_ALERT = "http://gtfs.ovapi.nl/nl/alerts.pb";

    String[] DIRECTION = {"OUTBOUND", "INBOUND"};

    String INCOMING_AT = "INCOMING_AT";
    String STOPPED_AT = "STOPPED_AT";
    String IN_TRANSIT_TO = "IN_TRANSIT_TO";

    String PATH_DUMMY_SM = "static/data/dummy_stop_monitoring.csv";

    String TIME_ZONE_NL = "Europe/Amsterdam";
}
