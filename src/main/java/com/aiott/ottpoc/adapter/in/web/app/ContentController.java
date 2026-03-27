package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.ContentViewResult;
import com.aiott.ottpoc.application.dto.RelatedContentResult;
import com.aiott.ottpoc.application.port.in.GetRelatedContentUseCase;
import com.aiott.ottpoc.application.port.in.GetContentViewUseCase;
import com.aiott.ottpoc.domain.model.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/app/contents")
@RequiredArgsConstructor
public class ContentController {
    private final GetRelatedContentUseCase getRelatedContentUseCase;
    private final GetContentViewUseCase getContentViewUseCase;

    @GetMapping("/{contentId}/related")
    public List<RelatedContentResult> related(
            @PathVariable UUID contentId,
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "TRAILER,CLIP,EXTRA") String types
    ) {
        List<ContentType> parsedTypes = List.of(types.split(","))
                .stream()
                .map(String::trim)
                .map(ContentType::valueOf)
                .toList();

        return getRelatedContentUseCase.getRelated(contentId, LangResolver.resolve(lang), parsedTypes);
    }

    @GetMapping("/{contentId}")
    public ContentViewResult get(
            @PathVariable UUID contentId,
            @RequestParam(required = false) String lang
    ) {
        return getContentViewUseCase.get(contentId, LangResolver.resolve(lang));
    }
}
