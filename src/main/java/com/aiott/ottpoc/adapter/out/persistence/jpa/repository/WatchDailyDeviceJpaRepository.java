package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.WatchDailyDeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface WatchDailyDeviceJpaRepository
        extends JpaRepository<WatchDailyDeviceJpaEntity, WatchDailyDeviceJpaEntity.Id> {

    @Modifying
    @Query(value = """
        insert into watch_daily_device (agg_date, content_id, device_id)
        values (?1, ?2, ?3)
        on conflict do nothing
        """, nativeQuery = true)
    int insertIgnore(LocalDate aggDate, UUID contentId, String deviceId);
}
