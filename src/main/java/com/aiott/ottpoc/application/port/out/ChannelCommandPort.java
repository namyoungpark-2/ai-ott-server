package com.aiott.ottpoc.application.port.out;

import java.util.UUID;

public interface ChannelCommandPort {
    UUID createChannel(UUID ownerId, String handle, String name, String description);
    void updateChannel(UUID channelId, String name, String description, String profileImageUrl, String bannerImageUrl);
    void updateChannelStatus(UUID channelId, String status);
    void incrementSubscriberCount(UUID channelId);
    void decrementSubscriberCount(UUID channelId);
}
