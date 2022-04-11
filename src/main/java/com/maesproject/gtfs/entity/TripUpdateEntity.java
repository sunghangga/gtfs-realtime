package com.maesproject.gtfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "trip_updates")
public class TripUpdateEntity {
    @Id
    @SequenceGenerator(name = "trip_updates_id_seq", sequenceName = "trip_updates_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trip_updates_id_seq")
    @Column(name = "id")
    private long id;
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
    @Column(name = "\"timestamp\"")
    private long timestamp;
    @Column(name = "delay")
    private int delay;
    @Column(name = "entity_id")
    private String entityId;
}
