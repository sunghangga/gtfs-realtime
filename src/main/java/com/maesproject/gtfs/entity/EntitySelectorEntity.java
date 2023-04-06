package com.maesproject.gtfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "entity_selectors")
public class EntitySelectorEntity {
    @Id
    @SequenceGenerator(name = "entity_selectors_id_seq", sequenceName = "entity_selectors_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_selectors_id_seq")
    @Column(name = "id")
    private int id;
    @Column(name = "agency_id")
    private String agencyId;
    @Column(name = "route_id")
    private String routeId;
    @Column(name = "route_type")
    private int routeType;
    @Column(name = "stop_id")
    private String stopId;
    @Column(name = "trip_id")
    private String tripId;
    @Column(name = "trip_route_id")
    private String tripRouteId;
    @Column(name = "direction_id")
    private int directionId;
    @Column(name = "trip_start_time")
    private String tripStartTime;
    @Column(name = "trip_start_date")
    private String tripStartDate;
    @Column(name = "schedule_relationship")
    private String scheduleRelationship;
    @Column(name = "alert_id")
    private long alertId;
}
