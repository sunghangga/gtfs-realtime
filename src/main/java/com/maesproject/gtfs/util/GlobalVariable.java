package com.maesproject.gtfs.util;

public interface GlobalVariable {
    // gtfs feed url
    String URL_TRIP_UPDATE = "http://gtfs.viainfo.net/tripupdate/tripupdates.pb";
    String URL_VEHICLE_POSITION = "http://gtfs.viainfo.net/vehicle/vehiclepositions.pb";
    String URL_ALERT = "http://gtfs.viainfo.net/alert/alerts.pb";

    // gtfs data type
    String GTFS_TRIP_UPDATE = "TRIP_UPDATE";
    String GTFS_VEHICLE_POSITION = "VEHICLE_POSITION";
    String GTFS_ALERT = "ALERT";

    String[] DIRECTION = {"OUTBOUND", "INBOUND"};

    String INCOMING_AT = "INCOMING_AT";
    String STOPPED_AT = "STOPPED_AT";
    String IN_TRANSIT_TO = "IN_TRANSIT_TO";

    String PATH_DUMMY_SM = "static/data/dummy_stop_monitoring.csv";

    String TIME_ZONE_NL = "Europe/Amsterdam";
}
