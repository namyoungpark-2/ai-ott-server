package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.channel.ChannelDetailResult;
import com.aiott.ottpoc.application.dto.channel.UpdateChannelCommand;
import com.aiott.ottpoc.application.port.in.CreatorChannelUseCase;
import com.aiott.ottpoc.application.port.out.ChannelCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorChannelService implements CreatorChannelUseCase {
    private final ChannelCommandPort channelCommandPort;
    private final ChannelQueryPort channelQueryPort;

    @Override
    @Transactional
    public ChannelDetailResult getOrCreateMyChannel(String userId, String lang) {
        UUID uid = UUID.fromString(userId);
        var existing = channelQueryPort.findByOwnerId(uid, lang);
        if (existing.isPresent()) {
            return existing.get();
        }
        // Auto-create channel with userId as handle (guaranteed unique since owner_id is unique)
        String handle = "user-" + userId.substring(0, 8);
        String name = handle;
        UUID channelId = channelCommandPort.createChannel(uid, handle, name, null);
        return channelQueryPort.findByOwnerId(uid, lang)
                .orElseThrow(() -> new RuntimeException("Failed to create channel"));
    }

    @Override
    @Transactional
    public ChannelDetailResult updateMyChannel(String userId, UpdateChannelCommand cmd) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));
        channelCommandPort.updateChannel(channelId, cmd.name(), cmd.description(), cmd.profileImageUrl(), cmd.bannerImageUrl());
        return channelQueryPort.findByOwnerId(uid, "en")
                .orElseThrow(() -> new RuntimeException("Channel not found after update"));
    }
}
