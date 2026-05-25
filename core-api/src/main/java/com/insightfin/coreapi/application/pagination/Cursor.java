package com.insightfin.coreapi.application.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Opaque pagination cursor pointing at a position in a {@code (createdAt, id)}
 * ordered list. Clients treat the encoded string as opaque — the server owns
 * the format and can evolve it without breaking the API thanks to the version
 * prefix.
 *
 * <p>Encoding: {@code v1.<base64url(json({"t": ISO-8601, "id": uuid}))>}.
 *
 * <p>The {@code id} tiebreak is what makes pagination stable when two rows
 * share the same {@code createdAt} — without it the sort order is
 * non-deterministic and the cursor would pull or skip rows on subsequent pages.
 */
public record Cursor(LocalDateTime createdAt, UUID id) {

    private static final String VERSION_PREFIX = "v1.";
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public String encode() {
        try {
            String json = MAPPER.writeValueAsString(new Payload(createdAt, id));
            String b64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
            return VERSION_PREFIX + b64;
        } catch (JsonProcessingException e) {
            // Cursor is built from already-valid data — serialization should never fail.
            throw new IllegalStateException("Failed to encode cursor", e);
        }
    }

    /**
     * @throws IllegalArgumentException if the input is null, missing the
     *                                  version prefix, or fails to decode/parse
     */
    public static Cursor decode(String raw) {
        if (raw == null || !raw.startsWith(VERSION_PREFIX)) {
            throw new IllegalArgumentException("invalid cursor");
        }
        String b64 = raw.substring(VERSION_PREFIX.length());
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(b64);
            Payload payload = MAPPER.readValue(new String(bytes, StandardCharsets.UTF_8), Payload.class);
            if (payload.t() == null || payload.id() == null) {
                throw new IllegalArgumentException("invalid cursor");
            }
            return new Cursor(payload.t(), payload.id());
        } catch (IllegalArgumentException e) {
            // Already an IAE (from us or from base64 decoder) — surface as-is.
            throw new IllegalArgumentException("invalid cursor", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid cursor", e);
        }
    }

    /** Internal JSON shape — keys kept short to keep the encoded cursor compact. */
    private record Payload(@JsonProperty("t") LocalDateTime t, @JsonProperty("id") UUID id) {}
}
