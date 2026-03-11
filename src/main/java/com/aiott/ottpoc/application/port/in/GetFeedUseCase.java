package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.FeedItemDto;
import java.util.List;

public interface GetFeedUseCase {
    List<FeedItemDto> getFeed(String lang);
}
