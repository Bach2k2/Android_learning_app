package com.example.japaneseapp.roleplay;

import com.example.japaneseapp.auth.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_conversation")
public class LearningConversation {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    private RoleplayCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", nullable = false)
    private RoleplayScenario scenario;

    @Column(nullable = false, length = 32)
    private String level;

    @Column(name = "target_language", nullable = false, length = 8)
    private String targetLanguage;

    @Column(name = "translation_language", nullable = false, length = 8)
    private String translationLanguage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected LearningConversation() {
    }

    public LearningConversation(UUID id, AppUser user, RoleplayCharacter character, RoleplayScenario scenario,
                               String level, String targetLanguage, String translationLanguage,
                               Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.user = user;
        this.character = character;
        this.scenario = scenario;
        this.level = level;
        this.targetLanguage = targetLanguage;
        this.translationLanguage = translationLanguage;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public RoleplayCharacter getCharacter() { return character; }
    public RoleplayScenario getScenario() { return scenario; }
    public String getLevel() { return level; }
    public String getTargetLanguage() { return targetLanguage; }
    public String getTranslationLanguage() { return translationLanguage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
