package com.example.japaneseapp.roleplay;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_message")
public class ConversationMessage {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private LearningConversation conversation;

    @Column(nullable = false, length = 16)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String reading;

    @Column(name = "client_message_id")
    private UUID clientMessageId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ConversationMessage() {
    }

    public ConversationMessage(UUID id, LearningConversation conversation, String role, String text,
                               String translation, String reading, UUID clientMessageId, Instant createdAt) {
        this.id = id;
        this.conversation = conversation;
        this.role = role;
        this.text = text;
        this.translation = translation;
        this.reading = reading;
        this.clientMessageId = clientMessageId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public LearningConversation getConversation() { return conversation; }
    public String getRole() { return role; }
    public String getText() { return text; }
    public String getTranslation() { return translation; }
    public String getReading() { return reading; }
    public UUID getClientMessageId() { return clientMessageId; }
    public Instant getCreatedAt() { return createdAt; }
}
