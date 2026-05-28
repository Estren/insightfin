package com.insightfin.coreapi.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CoachThreadResponse(
        UUID id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt
) {}
