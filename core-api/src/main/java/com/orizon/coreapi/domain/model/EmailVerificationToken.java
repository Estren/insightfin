package com.orizon.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailVerificationToken {

    private UUID id;
    private UUID userId;
    private String targetEmail;
    private String tokenHash;
    private String pinHash;
    private int pinAttempts;
    private EmailVerificationPurpose purpose;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    public EmailVerificationToken() {}

    public EmailVerificationToken(UUID id, UUID userId, String targetEmail, String tokenHash,
                                  String pinHash, int pinAttempts, EmailVerificationPurpose purpose,
                                  LocalDateTime expiresAt, LocalDateTime usedAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.targetEmail = targetEmail;
        this.tokenHash = tokenHash;
        this.pinHash = pinHash;
        this.pinAttempts = pinAttempts;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public int getPinAttempts() { return pinAttempts; }
    public void setPinAttempts(int pinAttempts) { this.pinAttempts = pinAttempts; }

    public EmailVerificationPurpose getPurpose() { return purpose; }
    public void setPurpose(EmailVerificationPurpose purpose) { this.purpose = purpose; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
