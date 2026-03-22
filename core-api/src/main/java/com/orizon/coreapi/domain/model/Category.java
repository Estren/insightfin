package com.orizon.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Category {

    private UUID id;
    private UUID userId;
    private String name;
    private TransactionType type;
    private String icon;
    private String color;
    private LocalDateTime createdAt;

    public Category() {}

    public Category(UUID id, UUID userId, String name, TransactionType type,
                    String icon, String color, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
