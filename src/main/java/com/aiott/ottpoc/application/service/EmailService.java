package com.aiott.ottpoc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스.
 *
 * app.mail.enabled=false (기본값)이면 SMTP를 사용하지 않고 콘솔 로그로 이메일 내용 출력.
 * app.mail.enabled=true  이면 JavaMailSender로 실제 발송 (SendGrid SMTP 릴레이 등 설정 필요).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@ai-ott.com}")
    private String from;

    @Value("${app.web.base-url:http://localhost:3000}")
    private String webBaseUrl;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // ─── 공개 API ─────────────────────────────────────────────────────────────

    public void sendVerificationEmail(String to, String token) {
        String link = webBaseUrl + "/auth/verify?token=" + token;
        String subject = "[AI-OTT] 이메일 주소를 인증해주세요";
        String body = buildVerificationBody(link);
        send(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = webBaseUrl + "/auth/reset-password?token=" + token;
        String subject = "[AI-OTT] 비밀번호 재설정 링크";
        String body = buildResetBody(link);
        send(to, subject, body);
    }

    // ─── 내부 구현 ────────────────────────────────────────────────────────────

    private void send(String to, String subject, String text) {
        if (!mailEnabled || mailSender == null) {
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("[EMAIL - DEV MODE] To: {}  Subject: {}", to, subject);
            log.info("[EMAIL BODY]\n{}", text);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("[EMAIL SENT] To={} Subject={}", to, subject);
        } catch (Exception e) {
            log.error("[EMAIL FAILED] To={} error={}", to, e.getMessage(), e);
        }
    }

    private String buildVerificationBody(String link) {
        return """
                AI-OTT에 가입해주셔서 감사합니다!

                아래 링크를 클릭해 이메일 주소를 인증해주세요:
                %s

                이 링크는 24시간 후 만료됩니다.

                본인이 가입하지 않았다면 이 이메일을 무시하셔도 됩니다.
                """.formatted(link);
    }

    private String buildResetBody(String link) {
        return """
                AI-OTT 비밀번호 재설정을 요청하셨습니다.

                아래 링크를 클릭해 비밀번호를 재설정해주세요:
                %s

                이 링크는 1시간 후 만료됩니다.

                비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시하셔도 됩니다.
                """.formatted(link);
    }
}
