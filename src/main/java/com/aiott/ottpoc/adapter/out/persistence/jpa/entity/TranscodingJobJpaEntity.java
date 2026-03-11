package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transcoding_job")
@Getter @Setter
public class TranscodingJobJpaEntity {

    @Id
    private UUID id;

    @Column(name="video_asset_id", nullable=false)
    private UUID videoAssetId;

    @Column(name="status", nullable=false, length=20)
    private String status; // QUEUED, RUNNING, SUCCEEDED, FAILED

    @Column(name="error_message", columnDefinition="text")
    private String errorMessage;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;
}
