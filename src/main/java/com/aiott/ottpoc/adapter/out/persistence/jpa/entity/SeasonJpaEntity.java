package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "season")
@Getter @Setter
public class SeasonJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private SeriesJpaEntity series;

    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    @Column(nullable = false)
    private String status;
}
