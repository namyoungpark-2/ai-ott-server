package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "watch_daily_device")
@Getter @Setter
public class WatchDailyDeviceJpaEntity {

    @EmbeddedId
    private Id id;

    @Embeddable
    @Getter @Setter
    @EqualsAndHashCode
    public static class Id {
        @Column(name = "agg_date", nullable = false)
        private LocalDate aggDate;

        @Column(name = "content_id", nullable = false)
        private java.util.UUID contentId;

        @Column(name = "device_id", nullable = false, length = 100)
        private String deviceId;
    }
}
