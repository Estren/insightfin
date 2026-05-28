package com.insightfin.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A single past message in a Coach conversation, hydrated from the Foundry
 * thread when the user reopens a conversation. Citations mirror the inline
 * sources surfaced during streaming.
 */
public record CoachMessage(
        String role,
        String text,
        List<Citation> citations,
        LocalDateTime createdAt
) {
    public record Citation(int marker, String filename) {}
}
