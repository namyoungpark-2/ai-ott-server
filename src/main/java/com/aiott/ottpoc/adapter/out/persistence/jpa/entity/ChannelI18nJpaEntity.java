package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "channel_i18n", uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "lang"}))
@Getter @Setter
public class ChannelI18nJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(nullable = false, length = 10)
    private String lang;

    @Column(length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
