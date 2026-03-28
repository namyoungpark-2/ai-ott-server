package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;

import java.util.List;
import java.util.UUID;

public interface AdminChannelUseCase {
    List<ChannelSummaryResult> listChannels(int limit);
    void updateChannelStatus(UUID channelId, String status);
}
