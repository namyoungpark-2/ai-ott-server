package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.channel.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelQueryPort {
    Optional<ChannelDetailResult> findByHandle(String handle, String lang);
    Optional<ChannelDetailResult> findByOwnerId(UUID ownerId, String lang);
    Optional<UUID> findChannelIdByOwnerId(UUID ownerId);
    Optional<UUID> findOfficialChannelId();
    List<ChannelSummaryResult> listAll(int limit);
    List<ChannelContentResult> listContentsByChannelHandle(String handle, String lang, int limit, int offset);
    List<ChannelSeriesResult> listSeriesByChannelHandle(String handle, String lang);
}
