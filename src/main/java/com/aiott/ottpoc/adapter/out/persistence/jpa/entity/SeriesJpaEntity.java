package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "series")
@Getter @Setter
public class SeriesJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String status;

    @Column(name = "default_language", nullable = false)
    private String defaultLanguage;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private ChannelJpaEntity channel;
}
