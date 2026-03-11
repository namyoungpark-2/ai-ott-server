package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.VideoAssetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface VideoAssetJpaRepository extends JpaRepository<VideoAssetJpaEntity, UUID> {
    Optional<VideoAssetJpaEntity> findFirstByContentIdAndStatus(UUID contentId, String status);


    @Query(value = """
        select
        va.id as video_asset_id,
        va.content_id,
        va.status,
        va.created_at,
        va.updated_at,

        (select count(*) from transcoding_job tj where tj.video_asset_id = va.id) as attempt_count,

        (select tj2.status
        from transcoding_job tj2
        where tj2.video_asset_id = va.id
        order by tj2.created_at desc
        limit 1) as latest_job_status,

        (select tj3.error_message
        from transcoding_job tj3
        where tj3.video_asset_id = va.id
        order by tj3.created_at desc
        limit 1) as latest_error_message

        from video_asset va
        where (?1 is null or va.status = ?1)
        order by va.created_at desc
        limit ?2
        """, nativeQuery = true)
    List<Object[]> adminListRaw(String status, int limit);


    @Query(value = """
    select
    va.id as video_asset_id,
    va.content_id,
    va.status,
    va.created_at,
    va.updated_at,

    va.source_key as source_url,
    case when va.status = 'READY'
      then concat('/hls/', va.id::text, '/master.m3u8')
      else null
    end as hls_url,
    case when va.id is not null
      then concat('/thumbnails/', va.id::text, '.jpg')
      else null
    end as thumbnail_url,

    (select count(*) from transcoding_job tj where tj.video_asset_id = va.id) as attempt_count,

    (select tj2.status
    from transcoding_job tj2
    where tj2.video_asset_id = va.id
    order by tj2.created_at desc
    limit 1) as latest_job_status,

    (select tj3.error_message
    from transcoding_job tj3
    where tj3.video_asset_id = va.id
    order by tj3.created_at desc
    limit 1) as latest_error_message

    from video_asset va
    where va.id = ?1
    """, nativeQuery = true)
    Object[] adminDetailRaw(UUID videoAssetId);


}
