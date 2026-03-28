package com.aiott.ottpoc.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthPort {
    boolean existsByUsername(String username);
    UserRecord save(String username, String passwordHash, String role);
    Optional<UserRecord> findByUsername(String username);

    // 이메일 인증
    void saveEmailVerificationToken(UUID userId, String token, java.time.OffsetDateTime expiresAt);
    Optional<UserRecord> findByEmailVerificationToken(String token);
    void markEmailVerified(UUID userId);

    // 비밀번호 재설정
    void savePasswordResetToken(UUID userId, String token, java.time.OffsetDateTime expiresAt);
    Optional<UserRecord> findByPasswordResetToken(String token);
    void updatePassword(UUID userId, String newPasswordHash);
    void clearPasswordResetToken(UUID userId);

    // 계정 관리
    Optional<UserRecord> findById(UUID userId);
    void updateUsername(UUID userId, String newUsername);
    void deleteUser(UUID userId);

    record UserRecord(
            UUID id,
            String username,
            String passwordHash,
            String role,
            String subscriptionTier,
            boolean emailVerified,
            String emailVerificationToken,
            java.time.OffsetDateTime emailVerificationExpiresAt,
            String passwordResetToken,
            java.time.OffsetDateTime passwordResetExpiresAt
    ) {}
}
