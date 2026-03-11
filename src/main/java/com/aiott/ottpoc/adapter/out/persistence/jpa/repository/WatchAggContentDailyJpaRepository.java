package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.WatchAggContentDailyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WatchAggContentDailyJpaRepository extends JpaRepository<WatchAggContentDailyJpaEntity, WatchAggContentDailyJpaEntity.Id> {

    @Modifying
    @Query(value = """
        insert into watch_agg_content_daily (agg_date, content_id, play_count, unique_devices, watch_time_ms, complete_count, updated_at)
        values (?1, ?2, ?3, ?4, ?5, ?6, now())
        on conflict (agg_date, content_id)
        do update set
          play_count = watch_agg_content_daily.play_count + excluded.play_count,
          unique_devices = watch_agg_content_daily.unique_devices + excluded.unique_devices,
          watch_time_ms = watch_agg_content_daily.watch_time_ms + excluded.watch_time_ms,
          complete_count = watch_agg_content_daily.complete_count + excluded.complete_count,
          updated_at = now()
        """, nativeQuery = true)
    void upsert(LocalDate aggDate, UUID contentId, int playInc, int uniqueInc, long watchIncMs, int completeInc);

    @Query(value = """
        select agg_date, play_count, unique_devices, watch_time_ms, complete_count
        from watch_agg_content_daily
        where content_id = ?1 and agg_date between ?2 and ?3
        order by agg_date asc
        """, nativeQuery = true)
    List<Object[]> findDaily(UUID contentId, LocalDate from, LocalDate to);

    @Query(value = """
        select content_id,
               sum(watch_time_ms) as watch_time_ms,
               sum(play_count) as play_count,
               sum(complete_count) as complete_count,
               sum(unique_devices) as unique_devices
        from watch_agg_content_daily
        where agg_date between ?1 and ?2
        group by content_id
        order by
          case when ?3 = 'watchTimeMs' then sum(watch_time_ms) end desc,
          case when ?3 = 'playCount' then sum(play_count) end desc,
          case when ?3 = 'completeCount' then sum(complete_count) end desc,
          case when ?3 = 'uniqueDevices' then sum(unique_devices) end desc,
          sum(watch_time_ms) desc
        limit ?4
        """, nativeQuery = true)
    List<Object[]> findTop(LocalDate from, LocalDate to, String metric, int limit);
}
