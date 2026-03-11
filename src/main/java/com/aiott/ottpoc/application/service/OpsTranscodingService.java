package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.OpsTranscodingUseCase;
import com.aiott.ottpoc.application.port.out.OpsTranscodingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpsTranscodingService implements OpsTranscodingUseCase {

    private final OpsTranscodingQueryPort queryPort;

    @Override
    public OpsTranscodingQueryPort.SummaryRow getSummary() {
        return queryPort.fetchSummary();
    }

    @Override
    public List<OpsTranscodingQueryPort.FailureTopRow> getFailureTop(int limit) {
        return queryPort.fetchFailureTop(limit);
    }

    @Override
    public List<OpsTranscodingQueryPort.RecentRow> getRecent(int limit) {
        return queryPort.fetchRecent(limit);
    }
}
