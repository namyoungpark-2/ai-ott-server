package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.UserJpaEntity;
import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.UserJpaRepository;
import com.aiott.ottpoc.application.port.out.UserAuthPort;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserAuthPersistenceAdapter implements UserAuthPort {

    private final UserJpaRepository repo;

    public UserAuthPersistenceAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    @Override
    public UserRecord save(String username, String passwordHash, String role) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        entity.setRole(role);
        entity.setSubscriptionTier("FREE");
        entity.setEmailVerified(false);
        entity.setCreatedAt(OffsetDateTime.now());
        UserJpaEntity saved = repo.save(entity);
        return toRecord(saved);
    }

    @Override
    public Optional<UserRecord> findByUsername(String username) {
        return repo.findByUsername(username).map(this::toRecord);
    }

    @Override
    public void saveEmailVerificationToken(UUID userId, String token, OffsetDateTime expiresAt) {
        repo.findById(userId).ifPresent(u -> {
            u.setEmailVerificationToken(token);
            u.setEmailVerificationExpiresAt(expiresAt);
            repo.save(u);
        });
    }

    @Override
    public Optional<UserRecord> findByEmailVerificationToken(String token) {
        return repo.findByEmailVerificationToken(token).map(this::toRecord);
    }

    @Override
    public void markEmailVerified(UUID userId) {
        repo.findById(userId).ifPresent(u -> {
            u.setEmailVerified(true);
            u.setEmailVerificationToken(null);
            u.setEmailVerificationExpiresAt(null);
            repo.save(u);
        });
    }

    @Override
    public void savePasswordResetToken(UUID userId, String token, OffsetDateTime expiresAt) {
        repo.findById(userId).ifPresent(u -> {
            u.setPasswordResetToken(token);
            u.setPasswordResetExpiresAt(expiresAt);
            repo.save(u);
        });
    }

    @Override
    public Optional<UserRecord> findByPasswordResetToken(String token) {
        return repo.findByPasswordResetToken(token).map(this::toRecord);
    }

    @Override
    public void updatePassword(UUID userId, String newPasswordHash) {
        repo.findById(userId).ifPresent(u -> {
            u.setPasswordHash(newPasswordHash);
            repo.save(u);
        });
    }

    @Override
    public void clearPasswordResetToken(UUID userId) {
        repo.findById(userId).ifPresent(u -> {
            u.setPasswordResetToken(null);
            u.setPasswordResetExpiresAt(null);
            repo.save(u);
        });
    }

    @Override
    public Optional<UserRecord> findById(UUID userId) {
        return repo.findById(userId).map(this::toRecord);
    }

    @Override
    public void updateUsername(UUID userId, String newUsername) {
        repo.findById(userId).ifPresent(u -> {
            u.setUsername(newUsername);
            repo.save(u);
        });
    }

    @Override
    public void deleteUser(UUID userId) {
        repo.deleteById(userId);
    }

    private UserRecord toRecord(UserJpaEntity e) {
        return new UserRecord(
                e.getId(),
                e.getUsername(),
                e.getPasswordHash(),
                e.getRole(),
                e.getSubscriptionTier() != null ? e.getSubscriptionTier() : "FREE",
                e.isEmailVerified(),
                e.getEmailVerificationToken(),
                e.getEmailVerificationExpiresAt(),
                e.getPasswordResetToken(),
                e.getPasswordResetExpiresAt()
        );
    }
}
