package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "content")
@Getter @Setter
public class ContentJpaEntity {

    @Id
    private UUID id;

    @Column(name="content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="series_id")
    private SeriesJpaEntity series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="season_id")
    private SeasonJpaEntity season;

    @Column(name="episode_number")
    private Integer episodeNumber;

    @Column(name="runtime_seconds")
    private Integer runtimeSeconds;

    @Column(name="release_at")
    private OffsetDateTime releaseAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="source_content_id")
    private ContentJpaEntity sourceContent;

    @Column(name="source_relation")
    private String sourceRelation;
}
