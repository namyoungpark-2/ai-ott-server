package com.aiott.ottpoc.application.port.in;

import java.util.UUID;

public interface AdminContentStatusUseCase {
    void changeStatus(UUID contentId, String status);
}
