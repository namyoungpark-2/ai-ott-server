package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.channel.ChannelDetailResult;
import com.aiott.ottpoc.application.dto.channel.ChannelContentResult;
import com.aiott.ottpoc.application.dto.channel.ChannelSeriesResult;

import java.util.List;

public interface ChannelPublicUseCase {
    List<ChannelDetailResult> listChannels(String lang, int limit, int offset);
    ChannelDetailResult getChannel(String handle, String lang);
    List<ChannelContentResult> getChannelContents(String handle, String lang, int limit, int offset);
    List<ChannelSeriesResult> getChannelSeries(String handle, String lang);
}
