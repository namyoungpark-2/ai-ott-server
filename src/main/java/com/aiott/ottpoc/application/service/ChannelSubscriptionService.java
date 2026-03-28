package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;
import com.aiott.ottpoc.application.port.in.ChannelSubscriptionUseCase;
import com.aiott.ottpoc.application.port.out.ChannelCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import com.aiott.ottpoc.application.port.out.ChannelSubscriptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelSubscriptionService implements ChannelSubscriptionUseCase {
    private final ChannelSubscriptionPort subscriptionPort;
    private final ChannelQueryPort channelQueryPort;
    private final ChannelCommandPort channelCommandPort;

    @Override
    @Transactional
    public void subscribe(String userId, String channelHandle) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findByHandle(channelHandle, "ko")
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelHandle))
                .id();
        if (!subscriptionPort.isSubscribed(uid, channelId)) {
            subscriptionPort.subscribe(uid, channelId);
            channelCommandPort.incrementSubscriberCount(channelId);
        }
    }

    @Override
    @Transactional
    public void unsubscribe(String userId, String channelHandle) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findByHandle(channelHandle, "ko")
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelHandle))
                .id();
        if (subscriptionPort.isSubscribed(uid, channelId)) {
            subscriptionPort.unsubscribe(uid, channelId);
            channelCommandPort.decrementSubscriberCount(channelId);
        }
    }

    @Override
    public boolean isSubscribed(String userId, String channelHandle) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findByHandle(channelHandle, "ko")
                .map(ch -> ch.id())
                .orElse(null);
        if (channelId == null) return false;
        return subscriptionPort.isSubscribed(uid, channelId);
    }

    @Override
    public List<ChannelSummaryResult> listMySubscriptions(String userId, String lang) {
        UUID uid = UUID.fromString(userId);
        return subscriptionPort.listSubscriptions(uid, lang);
    }
}
