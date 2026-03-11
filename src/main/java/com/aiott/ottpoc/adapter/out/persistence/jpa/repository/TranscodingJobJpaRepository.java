package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.TranscodingJobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface TranscodingJobJpaRepository extends JpaRepository<TranscodingJobJpaEntity, UUID> {

    @Query(value = """
        select *
        from transcoding_job
        where video_asset_id = ?1
        order by created_at desc
        limit 1
        """, nativeQuery = true)
    Optional<TranscodingJobJpaEntity> findLatest(UUID videoAssetId);

    @Query(value = """
        select count(*)
        from transcoding_job
        where video_asset_id = ?1
        """, nativeQuery = true)
    long countByVideoAssetId(UUID videoAssetId);

    @Query(value = """
        select exists(
          select 1 from transcoding_job
          where video_asset_id = ?1
            and status in ('QUEUED','RUNNING')
        )
        """, nativeQuery = true)
    boolean existsActiveJob(UUID videoAssetId);

    @Query(value = """
    select
      count(*) as total_jobs,
      sum(case when status='SUCCEEDED' then 1 else 0 end) as success_count,
      sum(case when status='FAILED' then 1 else 0 end) as failed_count,
      sum(case when status in ('QUEUED','RUNNING') then 1 else 0 end) as running_count,
      avg(extract(epoch from (updated_at - created_at))) as avg_processing_seconds
    from transcoding_job
    """, nativeQuery = true)
    List<Object[]> fetchSummaryRaw();


    @Query(value = """
        select error_message, count(*)
        from transcoding_job
        where status='FAILED'
          and error_message is not null
        group by error_message
        order by count(*) desc
        limit ?1
        """, nativeQuery = true)
    List<Object[]> fetchFailureTopRaw(int limit);


    @Query(value = """
        select id, video_asset_id, status, error_message, created_at, updated_at
        from transcoding_job
        order by created_at desc
        limit ?1
        """, nativeQuery = true)
    List<Object[]> fetchRecentRaw(int limit);
}
