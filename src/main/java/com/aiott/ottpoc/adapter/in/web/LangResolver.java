package com.aiott.ottpoc.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * Accept-Language 헤더 기반 언어 결정.
 * 우선순위: Accept-Language 헤더 → lang 쿼리 파라미터 → "en"
 */
public final class LangResolver {

    private LangResolver() {}

    public static String resolve(String langParam) {
        // 1. Accept-Language 헤더 확인
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String acceptLang = request.getHeader("Accept-Language");
            if (acceptLang != null && !acceptLang.isBlank()) {
                // "ko-KR,ko;q=0.9,en;q=0.8" → 첫 번째 언어의 primary tag 추출
                String primary = Locale.LanguageRange.parse(acceptLang).stream()
                        .findFirst()
                        .map(lr -> {
                            String range = lr.getRange();
                            // "*" 인 경우 무시
                            if ("*".equals(range)) return null;
                            int dash = range.indexOf('-');
                            return dash > 0 ? range.substring(0, dash) : range;
                        })
                        .orElse(null);
                if (primary != null && !primary.isBlank()) {
                    return primary;
                }
            }
        }

        // 2. 쿼리 파라미터 fallback
        if (langParam != null && !langParam.isBlank()) {
            return langParam;
        }

        // 3. 기본값
        return "en";
    }
}
