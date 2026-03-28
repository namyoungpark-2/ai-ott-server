package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;
import com.aiott.ottpoc.application.port.out.ChannelSubscriptionPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChannelSubscriptionPersistenceAdapter implements ChannelSubscriptionPort {
    private final EntityManager em;

    @Override
    @Transactional
    public void subscribe(UUID subscriberId, UUID channelId) {
        em.createNativeQuery("""
            INSERT INTO channel_subscription (subscriber_id, channel_id, created_at)
            VALUES (:subscriberId, :channelId, now())
            ON CONFLICT (subscriber_id, channel_id) DO NOTHING
        """)
        .setParameter("subscriberId", subscriberId)
        .setParameter("channelId", channelId)
        .executeUpdate();
    }

    @Override
    @Transactional
    public void unsubscribe(UUID subscriberId, UUID channelId) {
        em.createNativeQuery("DELETE FROM channel_subscription WHERE subscriber_id = :subscriberId AND channel_id = :channelId")
        .setParameter("subscriberId", subscriberId)
        .setParameter("channelId", channelId)
        .executeUpdate();
    }

    @Override
    public boolean isSubscribed(UUID subscriberId, UUID channelId) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
            "SELECT 1 FROM channel_subscription WHERE subscriber_id = :subscriberId AND channel_id = :channelId")
        .setParameter("subscriberId", subscriberId)
        .setParameter("channelId", channelId)
        .getResultList();
        return !rows.isEmpty();
    }

    @Override
    public List<ChannelSummaryResult> listSubscriptions(UUID subscriberId, String lang) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT c.id, c.handle, COALESCE(ci.name, c.name), c.profile_image_url,
                   c.is_official, c.subscriber_count, c.status
            FROM channel_subscription cs
            JOIN channel c ON c.id = cs.channel_id
            LEFT JOIN channel_i18n ci ON ci.channel_id = c.id AND ci.lang = :lang
            WHERE cs.subscriber_id = :subscriberId
            ORDER BY cs.created_at DESC
        """)
        .setParameter("subscriberId", subscriberId)
        .setParameter("lang", lang)
        .getResultList();

        return rows.stream().map(r -> new ChannelSummaryResult(
            (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
            (Boolean) r[4], ((Number) r[5]).intValue(), (String) r[6]
        )).toList();
    }
}
