package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;

import java.util.List;
import java.util.UUID;

public interface ChannelSubscriptionPort {
    void subscribe(UUID subscriberId, UUID channelId);
    void unsubscribe(UUID subscriberId, UUID channelId);
    boolean isSubscribed(UUID subscriberId, UUID channelId);
    List<ChannelSummaryResult> listSubscriptions(UUID subscriberId, String lang);
}
