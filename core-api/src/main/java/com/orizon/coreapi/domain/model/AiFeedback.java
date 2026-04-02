package com.orizon.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AiFeedback {

    private UUID id;
    private UUID userId;
    private AiFeedbackType type;
    private String title;
    private String content;
    private String metadata;
    private String referenceMonth;
    private boolean read;
    private LocalDateTime createdAt;

    public AiFeedback() {}

    public AiFeedback(UUID id, UUID userId, AiFeedbackType type, String title, String content,
                      String metadata, String referenceMonth, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.metadata = metadata;
        this.referenceMonth = referenceMonth;
        this.read = read;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public AiFeedbackType getType() { return type; }
    public void setType(AiFeedbackType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getReferenceMonth() { return referenceMonth; }
    public void setReferenceMonth(String referenceMonth) { this.referenceMonth = referenceMonth; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
