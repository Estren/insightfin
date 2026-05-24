package com.orizon.coreapi.application.pagination;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationParamsTest {

    // --- PP1: null limit → default ---
    @Test
    void fromQuery_nullLimit_defaultsTo20() {
        PaginationParams params = PaginationParams.fromQuery(null, null);

        assertThat(params.limit()).isEqualTo(PaginationParams.DEFAULT_LIMIT);
        assertThat(params.cursor()).isNull();
    }

    // --- PP2: explicit limit within range is preserved ---
    @Test
    void fromQuery_validLimit_isPreserved() {
        PaginationParams params = PaginationParams.fromQuery(50, null);

        assertThat(params.limit()).isEqualTo(50);
    }

    // --- PP3: limit < 1 → 400 ---
    @Test
    void fromQuery_limitZero_throwsBadRequest() {
        assertThatThrownBy(() -> PaginationParams.fromQuery(0, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("limit must be between 1 and 100");
    }

    // --- PP4: limit > MAX → 400 (explicit, not silently capped) ---
    @Test
    void fromQuery_limitAboveMax_throwsBadRequest() {
        assertThatThrownBy(() -> PaginationParams.fromQuery(101, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("limit must be between 1 and 100");
    }

    // --- PP5: limit at the boundaries (1 and MAX) is accepted ---
    @Test
    void fromQuery_limitAtBoundaries_isAccepted() {
        assertThat(PaginationParams.fromQuery(1, null).limit()).isEqualTo(1);
        assertThat(PaginationParams.fromQuery(PaginationParams.MAX_LIMIT, null).limit())
                .isEqualTo(PaginationParams.MAX_LIMIT);
    }

    // --- PP6: blank cursor is treated as missing (not as invalid) ---
    @Test
    void fromQuery_blankCursor_isTreatedAsNoCursor() {
        PaginationParams params = PaginationParams.fromQuery(20, "");

        assertThat(params.cursor()).isNull();
    }

    // --- PP7: valid encoded cursor is decoded ---
    @Test
    void fromQuery_validCursor_isDecoded() {
        Cursor original = new Cursor(LocalDateTime.of(2026, 5, 23, 14, 0), UUID.randomUUID());

        PaginationParams params = PaginationParams.fromQuery(20, original.encode());

        assertThat(params.cursor()).isEqualTo(original);
    }

    // --- PP8: invalid cursor → 400 with generic message (no internal format leakage) ---
    @Test
    void fromQuery_invalidCursor_throwsBadRequest() {
        assertThatThrownBy(() -> PaginationParams.fromQuery(20, "garbage"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invalid cursor");
    }
}
