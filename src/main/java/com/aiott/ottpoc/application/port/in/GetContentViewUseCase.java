package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.ContentViewResult;

import java.util.UUID;

public interface GetContentViewUseCase {
    ContentViewResult get(UUID contentId, String lang);
}
