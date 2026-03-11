package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.FeedItemDto;
import java.util.List;

public interface FeedQueryPort {
    List<FeedItemDto> loadFeed(String lang);
}
