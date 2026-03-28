package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.channel.CreatorSeriesResult;
import com.aiott.ottpoc.application.dto.channel.CreatorCreateSeriesCommand;

import java.util.List;
import java.util.UUID;

public interface CreatorSeriesUseCase {
    CreatorSeriesResult createSeries(String userId, CreatorCreateSeriesCommand cmd);
    List<CreatorSeriesResult> listMySeries(String userId, String lang);
    void updateSeries(String userId, UUID seriesId, String title, String description, String lang);
}
