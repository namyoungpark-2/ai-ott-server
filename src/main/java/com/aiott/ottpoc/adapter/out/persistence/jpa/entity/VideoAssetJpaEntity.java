package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="video_asset")
@Getter @Setter
public class VideoAssetJpaEntity {

    @Id
    private UUID id;

    @Column(name="content_id", nullable=false)
    private UUID contentId;

    @Column(nullable=false)
    private String storage;

    @Column(name="source_key", nullable=false, length=500)
    private String sourceKey;

    @Column(name="hls_master_key", length=500)
    private String hlsMasterKey;

    @Column(nullable=false)
    private String status;

    @Column(name="error_message", columnDefinition="text")
    private String errorMessage;

    @Column(name="video_width")
    private Integer videoWidth;

    @Column(name="video_height")
    private Integer videoHeight;

    @Column(name="duration_ms")
    private Long durationMs;

    @Column(name="created_at")
    private OffsetDateTime createdAt;

    @Column(name="updated_at")
    private OffsetDateTime updatedAt;
}
