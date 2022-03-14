package com.maesproject.gtfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stop_time_updates")
public class StopTimeUpdateEntity {
    @Id
    @SequenceGenerator(name = "stop_time_updates_id_seq", sequenceName = "stop_time_updates_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stop_time_updates_id_seq")
    @Column(name = "id")
    private int id;
    @Column(name = "stop_sequence")
    private int stopSequence;
    @Column(name = "stop_id")
    private String stopId;
    @Column(name = "arrival_delay")
    private int arrivalDelay;
    @Column(name = "arrival_time")
    private long arrivalTime;
    @Column(name = "arrival_uncertainty")
    private int arrivalUncertainty;
    @Column(name = "departure_delay")
    private int departureDelay;
    @Column(name = "departure_time")
    private long departureTime;
    @Column(name = "departure_uncertainty")
    private int departureUncertainty;
    @Column(name = "schedule_relationship")
    private String scheduleRelationship;
    @Column(name = "trip_update_id")
    private int tripUpdateId;
}
