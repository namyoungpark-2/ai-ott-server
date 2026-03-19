package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "subscription_tier", nullable = false, length = 20)
    private String subscriptionTier = "FREE";

    // ── 이메일 인증 ──────────────────────────────────────────────────────────

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private OffsetDateTime emailVerificationExpiresAt;

    // ── 비밀번호 재설정 ───────────────────────────────────────────────────────

    @Column(name = "password_reset_token", length = 64)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private OffsetDateTime passwordResetExpiresAt;

    // ── 공통 ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
