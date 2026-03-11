package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.SeriesJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SeriesJpaRepository
        extends JpaRepository<SeriesJpaEntity, UUID> {
}
