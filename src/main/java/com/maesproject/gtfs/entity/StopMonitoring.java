package com.maesproject.gtfs.entity;

import com.maesproject.gtfs.compositeid.StopMonitoringCompositeId;
import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import lombok.Data;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Duration;

@Data
@Entity
@IdClass(StopMonitoringCompositeId.class)
@Table(name = "stop_monitoring")
@TypeDef(typeClass = PostgreSQLIntervalType.class, defaultForType = Duration.class)
public class StopMonitoring {
    @Id
    @Column(name = "agency_id")
    private String agencyId;
    @Id
    @Column(name = "route_id")
    private String routeId;
    @Id
    @Column(name = "trip_id")
    private String tripId;
    @Id
    @Column(name = "direction_id")
    private int directionId;
    @Id
    @Column(name = "stop_id")
    private String stopId;
    @Id
    @Column(name = "stop_sequence")
    private int stopSequence;

    @Column(name = "vehicle_label")
    private String vehicleLabel;
    @Column(name = "\"timestamp\"")
    private long timestamp;
    @Column(name = "route_long_name")
    private String routeLongName;
    @Column(name = "origin_stop_id")
    private String originStopId;
    @Column(name = "origin_stop_name")
    private String originStopName;
    @Column(name = "destination_stop_id")
    private String destinationStopId;
    @Column(name = "destination_stop_name")
    private String destinationStopName;
    @Column(name = "congestion_level")
    private String congestionLevel;
    @Column(name = "position_longitude")
    private double positionLongitude;
    @Column(name = "position_latitude")
    private double positionLatitude;
    @Column(name = "position_bearing")
    private double positionBearing;
    @Column(name = "occupancy_status")
    private String occupancyStatus;
    @Column(name = "stop_name")
    private String stopName;
    @Column(name = "stop_lon")
    private String stopLon;
    @Column(name = "stop_lat")
    private String stopLat;
    @Column(name = "aimed_arrival_time", columnDefinition = "interval")
    private Duration aimedArrivalTime;
    @Column(name = "expected_arrival_time")
    private long expectedArrivalTime;
    @Column(name = "aimed_departure_time", columnDefinition = "interval")
    private Duration aimedDepartureTime;
    @Column(name = "expected_departure_time")
    private long expectedDepartureTime;
    @Column(name = "trip_start_date")
    private String tripStartDate;
    @Column(name = "current_status")
    private String currentStatus;
}
