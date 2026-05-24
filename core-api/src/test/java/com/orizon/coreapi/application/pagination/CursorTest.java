package com.orizon.coreapi.application.pagination;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorTest {

    // --- C1: encode/decode round-trips losslessly ---
    @Test
    void encodeDecode_roundTripsLosslessly() {
        Cursor original = new Cursor(LocalDateTime.of(2026, 5, 23, 14, 30, 45), UUID.randomUUID());

        Cursor decoded = Cursor.decode(original.encode());

        assertThat(decoded).isEqualTo(original);
    }

    // --- C2: encoded form is the documented v1.<base64url> shape ---
    @Test
    void encode_producesVersionedBase64UrlString() {
        String encoded = new Cursor(LocalDateTime.now(), UUID.randomUUID()).encode();

        assertThat(encoded).startsWith("v1.");
        // base64url alphabet only: A-Z a-z 0-9 - _
        assertThat(encoded.substring(3)).matches("[A-Za-z0-9_-]+");
    }

    // --- C3: null is rejected ---
    @Test
    void decode_null_throws() {
        assertThatThrownBy(() -> Cursor.decode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid cursor");
    }

    // --- C4: missing version prefix is rejected ---
    @Test
    void decode_withoutVersionPrefix_throws() {
        assertThatThrownBy(() -> Cursor.decode("eyJ0IjoiMjAyNi0wNS0yMyJ9"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid cursor");
    }

    // --- C5: malformed base64 is rejected ---
    @Test
    void decode_malformedBase64_throws() {
        assertThatThrownBy(() -> Cursor.decode("v1.!!not-base64!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid cursor");
    }

    // --- C6: valid base64 but invalid JSON is rejected ---
    @Test
    void decode_validBase64ButInvalidJson_throws() {
        // "garbage" in base64url is "Z2FyYmFnZQ"
        assertThatThrownBy(() -> Cursor.decode("v1.Z2FyYmFnZQ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid cursor");
    }

    // --- C7: JSON missing required fields is rejected ---
    @Test
    void decode_jsonMissingFields_throws() {
        // {} in base64url is "e30"
        assertThatThrownBy(() -> Cursor.decode("v1.e30"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid cursor");
    }
}
