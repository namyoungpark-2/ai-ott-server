package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.TranscodingJobJpaRepository;
import com.aiott.ottpoc.application.port.out.OpsTranscodingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpsTranscodingQueryAdapter implements OpsTranscodingQueryPort {

    private final TranscodingJobJpaRepository repo;

    @Override
    public SummaryRow fetchSummary() {
        Object[] r = repo.fetchSummaryRaw().get(0);

        long total   = r[0] == null ? 0 : ((Number) r[0]).longValue();
        long success = r[1] == null ? 0 : ((Number) r[1]).longValue();
        long failed  = r[2] == null ? 0 : ((Number) r[2]).longValue();
        long running = r[3] == null ? 0 : ((Number) r[3]).longValue();
        Double avg   = r[4] == null ? 0.0 : ((Number) r[4]).doubleValue();

        return new SummaryRow(total, success, failed, running, avg);
    }

    @Override
    public List<FailureTopRow> fetchFailureTop(int limit) {
        return repo.fetchFailureTopRaw(limit)
                .stream()
                .map(r -> new FailureTopRow((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @Override
    public List<RecentRow> fetchRecent(int limit) {
        return repo.fetchRecentRaw(limit)
                .stream()
                .map(r -> new RecentRow(
                        (UUID) r[0],
                        (UUID) r[1],
                        (String) r[2],
                        (String) r[3],
                        (OffsetDateTime) r[4],
                        (OffsetDateTime) r[5]
                ))
                .toList();
    }
}
