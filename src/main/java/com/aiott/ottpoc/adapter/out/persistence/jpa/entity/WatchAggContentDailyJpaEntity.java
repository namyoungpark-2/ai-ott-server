package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="watch_agg_content_daily")
@Getter @Setter
public class WatchAggContentDailyJpaEntity {

    @EmbeddedId
    private Id id;

    @Column(name="play_count", nullable=false)
    private int playCount;

    @Column(name="unique_devices", nullable=false)
    private int uniqueDevices;

    @Column(name="watch_time_ms", nullable=false)
    private long watchTimeMs;

    @Column(name="complete_count", nullable=false)
    private int completeCount;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Embeddable
    @Getter @Setter
    public static class Id {
        @Column(name="agg_date", nullable=false)
        private LocalDate aggDate;

        @Column(name="content_id", nullable=false)
        private UUID contentId;
    }
}
