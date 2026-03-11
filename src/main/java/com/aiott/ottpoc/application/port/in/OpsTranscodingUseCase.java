package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.port.out.OpsTranscodingQueryPort;

import java.util.List;

public interface OpsTranscodingUseCase {

    OpsTranscodingQueryPort.SummaryRow getSummary();
    List<OpsTranscodingQueryPort.FailureTopRow> getFailureTop(int limit);
    List<OpsTranscodingQueryPort.RecentRow> getRecent(int limit);
}
