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

        String action = resolveAuthAction(path);

        if (!"POST".equalsIgnoreCase(method) || action == null) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(req);
        String key = ip + "|" + action;

        // admin/ops 로그인도 포함한다 — 브루트포스 대상으로는 오히려 이쪽이 가치가 높다.
        boolean isLogin = action.equals("login")
                || action.equals("admin/login")
                || action.equals("ops/login");

        if (isLogin && isRateLimited(loginBuckets, key, LOGIN_LIMIT)) {
            log.warn("[RATE LIMIT] Login blocked for IP={} action={}", ip, action);
            sendTooManyRequests(res, "Too many login attempts. Please wait 1 minute.");
            return;
        }

        if (action.equals("signup") && isRateLimited(signupBuckets, key, SIGNUP_LIMIT)) {
            log.warn("[RATE LIMIT] Signup blocked for IP={}", ip);
            sendTooManyRequests(res, "Too many signup attempts. Please wait 1 minute.");
            return;
        }

        if (action.equals("forgot-password") && isRateLimited(forgotBuckets, key, FORGOT_LIMIT)) {
            log.warn("[RATE LIMIT] Forgot-password blocked for IP={}", ip);
            sendTooManyRequests(res, "Too many password reset attempts. Please wait 1 minute.");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * AuthController 는 {@code {"/auth", "/api/app/auth"}} 두 경로에 동시 매핑돼 있다.
     * 한쪽만 검사하면 다른 경로로 제한을 그대로 우회할 수 있으므로 액션명으로 정규화한다.
     *
     * @return 액션명(예: {@code "login"}), 인증 경로가 아니면 {@code null}
     */
    private static String resolveAuthAction(String path) {
        if (path.startsWith("/auth/")) {
            return path.substring("/auth/".length());
        }
        if (path.startsWith("/api/app/auth/")) {
            return path.substring("/api/app/auth/".length());
        }
        return null;
    }

    private boolean isRateLimited(Map<String, WindowEntry> buckets, String key, int limit) {
        long now = Instant.now().getEpochSecond();
        WindowEntry entry = buckets.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.windowStart) >= WINDOW_SECONDS) {
                return new WindowEntry(new AtomicInteger(1), now);
            }
            existing.count.incrementAndGet();
            return existing;
        });
        return entry.count.get() > limit;
    }

    private String resolveClientIp(HttpServletRequest req) {
        // CF-Connecting-IP 를 먼저 본다. Cloudflare 가 항상 실제 클라이언트 IP 로
        // 덮어쓰기 때문에 위조할 수 없다.
        //
        // X-Forwarded-For 를 먼저 보면 안 된다. 맨 앞 값은 클라이언트가 보낸 헤더가
        // 그대로 남아 있어, 요청마다 다른 값을 넣으면 IP별 버킷이 매번 새로 생기고
        // 레이트리밋이 무력화된다.
        String cfIp = req.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) {
            return cfIp.trim();
        }
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
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
