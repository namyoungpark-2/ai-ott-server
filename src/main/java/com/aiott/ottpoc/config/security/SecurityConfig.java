package com.aiott.ottpoc.config.security;

import com.aiott.ottpoc.config.StripeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 경로별 필터체인 3개로 분리한다. JwtTokenProvider가 토큰마다 다른 aud를
 * 심어주므로(admin / ops / app), 체인별로 요구 aud를 다르게 검증해
 * ops 토큰으로 admin API를 호출하는 식의 교차 사용을 막는다.
 *
 * <p>이전 구현은 단일 체인에 anyRequest().permitAll() 이어서 /api/admin/**가
 * 무인증 상태였다. @PreAuthorize 는 붙어 있어도 @EnableMethodSecurity 가 없어
 * 무시되고 있었다.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(StripeProperties.class)
public class SecurityConfig {

    private static final String AUTH_COOKIE = "auth_token";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 어드민 콘솔 전용. aud=admin + ROLE_ADMIN 둘 다 요구한다. */
    @Bean
    @Order(1)
    SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        return baseChain(http, "/api/admin/**", "admin")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .build();
    }

    /** 운영 지표 전용. ops 토큰은 ROLE_SRE 를 갖는다(JwtTokenProvider 참고). */
    @Bean
    @Order(2)
    SecurityFilterChain opsFilterChain(HttpSecurity http) throws Exception {
        return baseChain(http, "/api/ops/**", "ops")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("SRE"))
                .build();
    }

    /**
     * 사용자/크리에이터 앱 + 공개 엔드포인트.
     *
     * <p>규칙 순서가 중요하다. 인증 필수 경로를 공개 GET 규칙보다 먼저 선언해야
     * /api/app/channels/{handle}/subscription-status 처럼 공개 조회 경로 아래에
     * 있는 개인화 엔드포인트가 열리지 않는다.
     */
    @Bean
    @Order(3)
    SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception {
        return baseChain(http, null, "app")
                .authorizeHttpRequests(auth -> auth
                        // 헬스체크 — 외부 모니터링이 토큰 없이 호출한다
                        .requestMatchers("/", "/health", "/actuator/health").permitAll()
                        // 로그인·회원가입·비밀번호 재설정
                        .requestMatchers("/auth/**", "/api/app/auth/**").permitAll()
                        // Stripe 웹훅 — JWT 대신 서명으로 검증한다(StripeWebhookController)
                        .requestMatchers("/api/webhooks/**").permitAll()
                        // 로컬 스토리지 모드에서 서빙하는 미디어(WebConfig)
                        .requestMatchers("/hls/**", "/thumbnails/**", "/stream/**").permitAll()
                        // ── 인증 필수: 공개 GET 규칙보다 먼저 와야 한다 ──
                        .requestMatchers(
                                "/api/app/me/**",
                                "/api/app/creator/**",
                                "/api/app/payments/**",
                                "/api/app/playback/**",
                                "/api/app/watch-events/**",
                                "/api/app/analytics/**",
                                "/api/app/channels/*/subscribe",
                                "/api/app/channels/*/subscription-status"
                        ).authenticated()
                        // ── 공개 카탈로그 조회(읽기 전용) ──
                        .requestMatchers(HttpMethod.GET,
                                "/api/app/feed",
                                "/api/app/catalog/**",
                                "/api/app/contents/**",
                                "/api/app/series/**",
                                "/api/app/seasons/**",
                                "/api/app/channels/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    /**
     * 세 체인의 공통 설정. securityMatcher 가 null 이면 나머지 전부를 받는다.
     *
     * <p>CSRF 를 끈 상태를 유지하는 대신 auth_token 쿠키에 SameSite=Lax 를 걸어
     * 교차 사이트 요청에 쿠키가 실리지 않게 한다(AuthController#setAuthCookie).
     */
    private HttpSecurity baseChain(HttpSecurity http, String pathPattern, String requiredAud)
            throws Exception {
        if (pathPattern != null) {
            http.securityMatcher(pathPattern);
        }
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthConverter(requiredAud))))
                .addFilterBefore(
                        new CookieBearerTokenFilter(AUTH_COOKIE),
                        BearerTokenAuthenticationFilter.class);
    }
}
