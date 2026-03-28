package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.channel.CreatorContentResult;
import com.aiott.ottpoc.application.dto.channel.CreatorCreateContentCommand;
import com.aiott.ottpoc.application.dto.channel.CreatorContentSummary;

import java.util.List;
import java.util.UUID;

public interface CreatorContentUseCase {
    CreatorContentResult createContent(String userId, CreatorCreateContentCommand cmd);
    List<CreatorContentSummary> listMyContents(String userId, String lang, int limit);
    void updateContentMetadata(String userId, UUID contentId, String title, String description, String lang);
    void updateContentStatus(String userId, UUID contentId, String status);
    void deleteContent(String userId, UUID contentId);
}
