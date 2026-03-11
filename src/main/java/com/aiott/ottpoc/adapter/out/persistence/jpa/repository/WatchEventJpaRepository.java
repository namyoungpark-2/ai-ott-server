package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.WatchEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WatchEventJpaRepository extends JpaRepository<WatchEventJpaEntity, UUID> {}
