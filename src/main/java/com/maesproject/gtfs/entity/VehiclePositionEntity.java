package com.maesproject.gtfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "vehicle_positions")
public class VehiclePositionEntity {
    @Id
    @SequenceGenerator(name = "vehicle_positions_id_seq", sequenceName = "vehicle_positions_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_positions_id_seq")
    @Column(name = "id")
    private int id;
    @Column(name = "trip_id")
    private String tripId;
    @Column(name = "route_id")
    private String routeId;
    @Column(name = "direction_id")
    private int directionId;
    @Column(name = "trip_start_time")
    private String tripStartTime;
    @Column(name = "trip_start_date")
    private String tripStartDate;
    @Column(name = "schedule_relationship")
    private String scheduleRelationship;
    @Column(name = "vehicle_id")
    private String vehicleId;
    @Column(name = "vehicle_label")
    private String vehicleLabel;
    @Column(name = "vehicle_license_plate")
    private String vehicleLicensePlate;
    @Column(name = "position_latitude")
    private float positionLatitude;
    @Column(name = "position_longitude")
    private float positionLongitude;
    @Column(name = "position_bearing")
    private float positionBearing;
    @Column(name = "position_odometer")
    private double positionOdometer;
    @Column(name = "position_speed")
    private float positionSpeed;
    @Column(name = "current_stop_sequence")
    private int currentStopSequence;
    @Column(name = "stop_id")
    private String stopId;
    @Column(name = "current_status")
    private String currentStatus;
    @Column(name = "occupancy_status")
    private String occupancyStatus;
    @Column(name = "congestion_level")
    private String congestionLevel;
    @Column(name = "\"timestamp\"")
    private long timestamp;
    @Column(name = "entity_id")
    private String entityId;
}
