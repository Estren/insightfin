package com.insightfin.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata for one Coach Agent conversation. The actual messages live in the
 * Foundry thread referenced by {@code foundryThreadId}; we only store enough
 * to list/title/order conversations in the sidebar. The frontend never sees
 * {@code foundryThreadId} — it operates on our {@code id}.
 */
public class CoachThread {

    private UUID id;
    private UUID userId;
    private String foundryThreadId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public CoachThread() {}

    public CoachThread(UUID id, UUID userId, String foundryThreadId, String title,
                       LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.foundryThreadId = foundryThreadId;
        this.title = title;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getFoundryThreadId() { return foundryThreadId; }
    public void setFoundryThreadId(String foundryThreadId) { this.foundryThreadId = foundryThreadId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
