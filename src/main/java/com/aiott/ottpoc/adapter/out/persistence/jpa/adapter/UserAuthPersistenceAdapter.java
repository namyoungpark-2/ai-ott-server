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
        entity.setCreatedAt(OffsetDateTime.now());
        UserJpaEntity saved = repo.save(entity);
        return new UserRecord(saved.getId(), saved.getUsername(), saved.getPasswordHash(), saved.getRole());
    }

    @Override
    public Optional<UserRecord> findByUsername(String username) {
        return repo.findByUsername(username)
                .map(e -> new UserRecord(e.getId(), e.getUsername(), e.getPasswordHash(), e.getRole()));
    }
}
