package com.aiott.ottpoc.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthPort {
    boolean existsByUsername(String username);
    UserRecord save(String username, String passwordHash, String role);
    Optional<UserRecord> findByUsername(String username);

    record UserRecord(UUID id, String username, String passwordHash, String role) {}
}
