package com.insightfin.coreapi.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CoachMessageResponse(
        String role,
        String text,
        List<CitationResponse> citations,
        LocalDateTime createdAt
) {
    public record CitationResponse(int marker, String filename) {}
}
