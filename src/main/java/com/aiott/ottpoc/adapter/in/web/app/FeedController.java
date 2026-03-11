package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.dto.FeedItemDto;
import com.aiott.ottpoc.application.port.in.GetFeedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class FeedController {

    private final GetFeedUseCase getFeedUseCase;

    @GetMapping("/feed")
    public List<FeedItemDto> feed(@RequestParam(defaultValue = "en") String lang) {
        return getFeedUseCase.getFeed(lang);
    }
}
