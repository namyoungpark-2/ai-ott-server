package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.channel.ChannelContentResult;
import com.aiott.ottpoc.application.dto.channel.ChannelDetailResult;
import com.aiott.ottpoc.application.dto.channel.ChannelSeriesResult;
import com.aiott.ottpoc.application.port.in.ChannelPublicUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/channels")
public class ChannelPublicController {
    private final ChannelPublicUseCase useCase;

    @GetMapping
    public List<ChannelDetailResult> listChannels(
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return useCase.listChannels(LangResolver.resolve(lang), limit, offset);
    }

    @GetMapping("/{handle}")
    public ChannelDetailResult getChannel(
            @PathVariable String handle,
            @RequestParam(required = false) String lang) {
        return useCase.getChannel(handle, LangResolver.resolve(lang));
    }

    @GetMapping("/{handle}/contents")
    public List<ChannelContentResult> getContents(
            @PathVariable String handle,
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return useCase.getChannelContents(handle, LangResolver.resolve(lang), limit, offset);
    }

    @GetMapping("/{handle}/series")
    public List<ChannelSeriesResult> getSeries(
            @PathVariable String handle,
            @RequestParam(required = false) String lang) {
        return useCase.getChannelSeries(handle, LangResolver.resolve(lang));
    }
}
