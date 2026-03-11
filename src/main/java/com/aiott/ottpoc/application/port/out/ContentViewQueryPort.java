package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.ContentViewResult;

import java.util.UUID;

public interface ContentViewQueryPort {
    ContentViewResult findById(UUID contentId, String lang);
}
