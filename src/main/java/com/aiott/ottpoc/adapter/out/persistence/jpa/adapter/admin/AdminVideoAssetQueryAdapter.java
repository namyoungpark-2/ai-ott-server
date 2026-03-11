package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter.admin;

import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.VideoAssetJpaRepository;
import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetDetail;
import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetSummary;
import com.aiott.ottpoc.application.port.out.admin.AdminVideoAssetQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminVideoAssetQueryAdapter implements AdminVideoAssetQueryPort {

    private final VideoAssetJpaRepository repo;

    @Override
    public List<AdminVideoAssetSummary> findVideoAssets(String status, int limit) {
        String s = (status == null || status.isBlank()) ? null : status.toUpperCase();
        int l = (limit <= 0 || limit > 200) ? 50 : limit;

        return repo.adminListRaw(s, l).stream().map(r -> new AdminVideoAssetSummary(
                (UUID) r[0],
                (UUID) r[1],
                (String) r[2],
                ((Number) r[5]).longValue(),
                (String) r[6],
                (String) r[7],
                (OffsetDateTime) r[3],
                (OffsetDateTime) r[4]
        )).toList();
    }

    @Override
    public Optional<AdminVideoAssetDetail> findVideoAsset(UUID videoAssetId) {
        Object[] r = repo.adminDetailRaw(videoAssetId);
        if (r == null) return Optional.empty();

        return Optional.of(new AdminVideoAssetDetail(
                (UUID) r[0],
                (UUID) r[1],
                (String) r[2],
                (String) r[5],   // source_url
                (String) r[6],   // hls_url
                (String) r[7],   // thumbnail_url
                ((Number) r[8]).longValue(),
                (String) r[9],
                (String) r[10],
                (OffsetDateTime) r[3],
                (OffsetDateTime) r[4]
        ));
    }
}
