package com.aiott.ottpoc.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "content_i18n")
@Getter @Setter
public class ContentI18nJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="content_id", nullable=false)
    private UUID contentId;

    @Column(nullable=false)
    private String lang;

    @Column(nullable=false)
    private String title;

    @Column(columnDefinition="text")
    private String description;
}
