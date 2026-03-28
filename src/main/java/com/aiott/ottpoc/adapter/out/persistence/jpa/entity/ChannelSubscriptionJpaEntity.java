package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "channel_subscription")
@IdClass(ChannelSubscriptionJpaEntity.Id.class)
@Getter @Setter
public class ChannelSubscriptionJpaEntity {

    @jakarta.persistence.Id
    @Column(name = "subscriber_id")
    private UUID subscriberId;

    @jakarta.persistence.Id
    @Column(name = "channel_id")
    private UUID channelId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Getter @Setter
    public static class Id implements Serializable {
        private UUID subscriberId;
        private UUID channelId;

        public Id() {}
        public Id(UUID subscriberId, UUID channelId) {
            this.subscriberId = subscriberId;
            this.channelId = channelId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id that)) return false;
            return subscriberId.equals(that.subscriberId) && channelId.equals(that.channelId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(subscriberId, channelId);
        }
    }
}
