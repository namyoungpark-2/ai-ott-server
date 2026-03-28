package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.channel.ChannelDetailResult;
import com.aiott.ottpoc.application.dto.channel.UpdateChannelCommand;

public interface CreatorChannelUseCase {
    ChannelDetailResult getOrCreateMyChannel(String userId, String lang);
    ChannelDetailResult updateMyChannel(String userId, UpdateChannelCommand cmd);
}
