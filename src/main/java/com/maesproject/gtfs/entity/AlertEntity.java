package com.maesproject.gtfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "alerts")
public class AlertEntity {
    @Id
    @SequenceGenerator(name = "alerts_id_seq", sequenceName = "alerts_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alerts_id_seq")
    @Column(name = "id")
    private int id;
    @Column(name = "start")
    private long start;
    @Column(name = "\"end\"")
    private long end;
    @Column(name = "cause")
    private String cause;
    @Column(name = "effect")
    private String effect;
    @Column(name = "url")
    private String url;
    @Column(name = "header_text")
    private String headerText;
    @Column(name = "description_text")
    private String descriptionText;
    @Column(name = "entity_id")
    private String entityId;
}
