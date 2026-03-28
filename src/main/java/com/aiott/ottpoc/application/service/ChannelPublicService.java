package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.channel.ChannelContentResult;
import com.aiott.ottpoc.application.dto.channel.ChannelDetailResult;
import com.aiott.ottpoc.application.dto.channel.ChannelSeriesResult;
import com.aiott.ottpoc.application.port.in.ChannelPublicUseCase;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelPublicService implements ChannelPublicUseCase {
    private final ChannelQueryPort channelQueryPort;

    @Override
    public List<ChannelDetailResult> listChannels(String lang, int limit, int offset) {
        return channelQueryPort.listPublicChannels(lang, limit, offset);
    }

    @Override
    public ChannelDetailResult getChannel(String handle, String lang) {
        return channelQueryPort.findByHandle(handle, lang)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + handle));
    }

    @Override
    public List<ChannelContentResult> getChannelContents(String handle, String lang, int limit, int offset) {
        return channelQueryPort.listContentsByChannelHandle(handle, lang, limit, offset);
    }

    @Override
    public List<ChannelSeriesResult> getChannelSeries(String handle, String lang) {
        return channelQueryPort.listSeriesByChannelHandle(handle, lang);
    }
}
