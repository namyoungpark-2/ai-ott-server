package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.ChannelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChannelJpaRepository extends JpaRepository<ChannelJpaEntity, UUID> {
    Optional<ChannelJpaEntity> findByHandle(String handle);
    Optional<ChannelJpaEntity> findByOwnerId(UUID ownerId);
}
