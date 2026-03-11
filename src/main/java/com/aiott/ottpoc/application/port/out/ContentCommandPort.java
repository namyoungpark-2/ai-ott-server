package com.aiott.ottpoc.application.port.out;

import java.util.UUID;

public interface ContentCommandPort {
    UUID createMovieContent(String title); // MVP: movie로 생성
    void publish(UUID contentId);          // READY 시점에 공개 처리(선택)
}
