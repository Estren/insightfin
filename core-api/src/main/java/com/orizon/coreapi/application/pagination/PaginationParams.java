package com.orizon.coreapi.application.pagination;

import jakarta.ws.rs.BadRequestException;

/**
 * Validated pagination params ready for a service to consume. Built at the
 * boundary (controller) from raw query params via {@link #fromQuery}; any
 * invalid input becomes a 400 with a friendly message before the service ever
 * sees it.
 *
 * <p>{@code cursor} is already decoded — {@code null} means "start at the top".
 */
public record PaginationParams(int limit, Cursor cursor) {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public static PaginationParams fromQuery(Integer rawLimit, String rawCursor) {
        int limit = rawLimit == null ? DEFAULT_LIMIT : rawLimit;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + MAX_LIMIT);
        }

        Cursor cursor = null;
        if (rawCursor != null && !rawCursor.isBlank()) {
            try {
                cursor = Cursor.decode(rawCursor);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("invalid cursor");
            }
        }

        return new PaginationParams(limit, cursor);
    }
}
