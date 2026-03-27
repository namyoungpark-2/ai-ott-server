package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;
import com.aiott.ottpoc.application.port.in.AdminContentQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/failures")
public class AdminFailuresController {

    private final AdminContentQueryUseCase queryUseCase;

    /**
     * 프론트엔드 Failures 페이지용 – 모든 콘텐츠의 asset/job 상태를 포함하여 반환.
     * 프론트엔드에서 FAILED / PROCESSING 등을 필터링한다.
     */
    @GetMapping
    public List<FailureRow> list(
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "200") int limit
    ) {
        return queryUseCase.list(LangResolver.resolve(lang), null, limit)
                .stream()
                .map(FailureRow::from)
                .toList();
    }

    public record FailureRow(
            String contentId,
            String assetId,
            String title,
            String uiStatus,
            String assetStatus,
            String latestJobStatus,
            String errorMessage,
            long attemptCount,
            String updatedAt
    ) {
        static FailureRow from(AdminContentSummary s) {
            return new FailureRow(
                    s.contentId() != null ? s.contentId().toString() : null,
                    s.videoAssetId() != null ? s.videoAssetId().toString() : null,
                    s.title(),
                    s.uiStatus(),
                    s.videoAssetStatus(),
                    s.latestJobStatus(),
                    s.latestErrorMessage(),
                    s.attemptCount(),
                    s.updatedAt() != null ? s.updatedAt().toString() : null
            );
        }
    }
}
