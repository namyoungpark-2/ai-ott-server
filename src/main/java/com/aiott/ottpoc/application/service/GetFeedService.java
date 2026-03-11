package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.FeedItemDto;
import com.aiott.ottpoc.application.port.in.GetFeedUseCase;
import com.aiott.ottpoc.application.port.out.FeedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFeedService implements GetFeedUseCase {
    private final FeedQueryPort feedQueryPort;

    @Override
    public List<FeedItemDto> getFeed(String lang) {
        return feedQueryPort.loadFeed(lang);
    }
}
