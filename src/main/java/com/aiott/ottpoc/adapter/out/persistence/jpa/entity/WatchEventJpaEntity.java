package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "watch_event",
        uniqueConstraints = @UniqueConstraint(name = "uq_watch_event_client_event", columnNames = "client_event_id"))
@Getter @Setter
public class WatchEventJpaEntity {

    @Id
    private UUID id;

    @Column(name="client_event_id", nullable=false, length=100)
    private String clientEventId;

    @Column(name="event_type", nullable=false, length=20)
    private String eventType;

    @Column(name="occurred_at", nullable=false)
    private OffsetDateTime occurredAt;

    @Column(name="received_at", nullable=false)
    private OffsetDateTime receivedAt;

    @Column(name="content_id", nullable=false)
    private UUID contentId;

    @Column(name="video_asset_id")
    private UUID videoAssetId;

    @Column(name="session_id", nullable=false, length=100)
    private String sessionId;

    @Column(name="device_id", nullable=false, length=100)
    private String deviceId;

    @Column(name="position_ms", nullable=false)
    private int positionMs;

    @Column(name="delta_ms")
    private Integer deltaMs;

    @Column(name="duration_ms")
    private Integer durationMs;

    @Column(name="playback_rate")
    private Double playbackRate;

    @Column(length=2)
    private String country;

    @Column(length=20)
    private String player;

    @Column(name="app_version", length=30)
    private String appVersion;

    @Column(name="network_type", length=20)
    private String networkType;

    @Column(name="extra_json", columnDefinition="jsonb")
    private String extraJson;
}
