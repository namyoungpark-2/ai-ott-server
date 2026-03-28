package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;
import com.aiott.ottpoc.application.port.in.AdminChannelUseCase;
import com.aiott.ottpoc.application.port.out.ChannelCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminChannelService implements AdminChannelUseCase {
    private final ChannelQueryPort channelQueryPort;
    private final ChannelCommandPort channelCommandPort;

    @Override
    public List<ChannelSummaryResult> listChannels(int limit) {
        return channelQueryPort.listAll(limit);
    }

    @Override
    @Transactional
    public void updateChannelStatus(UUID channelId, String status) {
        channelCommandPort.updateChannelStatus(channelId, status);
    }
}
