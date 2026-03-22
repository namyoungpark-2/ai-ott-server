package com.aiott.ottpoc.config.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 인증 엔드포인트 무차별 대입 공격 방지용 인메모리 속도 제한 필터.
 *
 * 슬라이딩 윈도우 대신 고정 1분 윈도우를 사용해 단순하게 구현.
 * - /auth/login  : IP당 분당 5회
 * - /auth/signup : IP당 분당 3회
 * - /auth/forgot-password : IP당 분당 3회
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int LOGIN_LIMIT = 5;
    private static final int SIGNUP_LIMIT = 3;
    private static final int FORGOT_LIMIT = 3;
    private static final long WINDOW_SECONDS = 60L;

    private final Map<String, WindowEntry> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, WindowEntry> signupBuckets = new ConcurrentHashMap<>();
    private final Map<String, WindowEntry> forgotBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();
        String method = req.getMethod();

        if (!"POST".equalsIgnoreCase(method) || !path.startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(req);

        if (path.equals("/auth/login") && isRateLimited(loginBuckets, ip, LOGIN_LIMIT)) {
            log.warn("[RATE LIMIT] Login blocked for IP={}", ip);
            sendTooManyRequests(res, "Too many login attempts. Please wait 1 minute.");
            return;
        }

        if (path.equals("/auth/signup") && isRateLimited(signupBuckets, ip, SIGNUP_LIMIT)) {
            log.warn("[RATE LIMIT] Signup blocked for IP={}", ip);
            sendTooManyRequests(res, "Too many signup attempts. Please wait 1 minute.");
            return;
        }

        if (path.equals("/auth/forgot-password") && isRateLimited(forgotBuckets, ip, FORGOT_LIMIT)) {
            log.warn("[RATE LIMIT] Forgot-password blocked for IP={}", ip);
            sendTooManyRequests(res, "Too many password reset attempts. Please wait 1 minute.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isRateLimited(Map<String, WindowEntry> buckets, String ip, int limit) {
        long now = Instant.now().getEpochSecond();
        WindowEntry entry = buckets.compute(ip, (k, existing) -> {
            if (existing == null || (now - existing.windowStart) >= WINDOW_SECONDS) {
                return new WindowEntry(new AtomicInteger(1), now);
            }
            existing.count.incrementAndGet();
            return existing;
        });
        return entry.count.get() > limit;
    }

    private String resolveClientIp(HttpServletRequest req) {
        // Render, Cloudflare 등 프록시 뒤에서는 X-Forwarded-For 헤더를 사용
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String cfIp = req.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) {
            return cfIp.trim();
        }
        return req.getRemoteAddr();
    }

    private void sendTooManyRequests(HttpServletResponse res, String message) throws IOException {
        res.setStatus(429);
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Retry-After", "60");
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private static final class WindowEntry {
        final AtomicInteger count;
        final long windowStart;

        WindowEntry(AtomicInteger count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
