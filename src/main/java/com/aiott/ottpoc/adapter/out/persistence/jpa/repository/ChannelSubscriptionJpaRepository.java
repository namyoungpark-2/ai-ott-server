package com.aiott.ottpoc.adapter.out.persistence.jpa.repository;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.ChannelSubscriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChannelSubscriptionJpaRepository extends JpaRepository<ChannelSubscriptionJpaEntity, ChannelSubscriptionJpaEntity.Id> {
    boolean existsBySubscriberIdAndChannelId(UUID subscriberId, UUID channelId);
    void deleteBySubscriberIdAndChannelId(UUID subscriberId, UUID channelId);
}
