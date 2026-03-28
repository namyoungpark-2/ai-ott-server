package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;

import java.util.List;

public interface ChannelSubscriptionUseCase {
    void subscribe(String userId, String channelHandle);
    void unsubscribe(String userId, String channelHandle);
    boolean isSubscribed(String userId, String channelHandle);
    List<ChannelSummaryResult> listMySubscriptions(String userId, String lang);
}
