package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.ContentViewResult;
import com.aiott.ottpoc.application.port.in.GetContentViewUseCase;
import com.aiott.ottpoc.application.port.out.ContentViewQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetContentViewService implements GetContentViewUseCase {

    private final ContentViewQueryPort queryPort;

    @Override
    public ContentViewResult get(UUID contentId, String lang) {
        return queryPort.findById(contentId, lang);
    }
}
