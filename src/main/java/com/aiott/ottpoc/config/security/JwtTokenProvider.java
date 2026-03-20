package com.aiott.ottpoc.config.security;

import com.aiott.ottpoc.application.security.Permissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class JwtTokenProvider {

    private final String secret;

    public JwtTokenProvider(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret;
    }

    // ⚠️ POC용: HS256 직접 생성 (운영은 Nimbus/Jose로 발급 권장)
    public String issueAdminToken(String subject) {
        return issueToken(subject, "admin", List.of(
                Permissions.CONTENT_READ, Permissions.CONTENT_CREATE, Permissions.CONTENT_UPDATE,
                Permissions.CONTENT_PUBLISH, Permissions.CONTENT_ARCHIVE,
                Permissions.ASSET_READ, Permissions.ASSET_CREATE,
                Permissions.JOB_READ, Permissions.JOB_RETRY
        ), List.of("ROLE_ADMIN"));
    }

    public String issueAppToken(String subject, String role) {
        return issueToken(subject, "app", List.of(), List.of(role));
    }

    public String issueOpsToken(String subject) {
        return issueToken(subject, "ops", List.of(
                Permissions.METRICS_READ, Permissions.LOGS_READ,
                Permissions.ALERTS_READ, Permissions.ALERTS_UPDATE
        ), List.of("ROLE_SRE"));
    }

    private String issueToken(String sub, String aud, List<String> scopes, List<String> roles) {
        long now = Instant.now().getEpochSecond();
        long exp = now + 60 * 30; // 30분

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", sub);
        payload.put("aud", List.of(aud));
        payload.put("scopes", scopes);
        payload.put("roles", roles);
        payload.put("iat", now);
        payload.put("exp", exp);

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = toJson(payload);

        String header = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String body = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + body;
        String signature = hmacSha256(signingInput, secret);

        return signingInput + "." + signature;
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // 최소 JSON 직렬화(POC). 운영은 Jackson 사용 권장.
    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            sb.append(valueToJson(e.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String valueToJson(Object v) {
        if (v == null) return "null";
        if (v instanceof Number || v instanceof Boolean) return String.valueOf(v);
        if (v instanceof String s) return "\"" + s.replace("\"", "\\\"") + "\"";
        if (v instanceof Collection<?> c) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object x : c) {
                if (!first) sb.append(",");
                first = false;
                sb.append(valueToJson(x));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + String.valueOf(v).replace("\"", "\\\"") + "\"";
    }
}
