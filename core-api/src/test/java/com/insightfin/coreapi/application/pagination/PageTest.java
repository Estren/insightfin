package com.insightfin.coreapi.application.pagination;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PageTest {

    private static final Function<Item, Cursor> CURSOR_OF = i -> new Cursor(i.createdAt(), i.id());

    private record Item(UUID id, LocalDateTime createdAt, String label) {}

    private static Item item(int n) {
        // n=0 is the oldest, higher n is more recent (we sort DESC in real use)
        return new Item(UUID.randomUUID(), LocalDateTime.of(2026, 5, 23, 10, 0).plusMinutes(n), "item-" + n);
    }

    // --- P1: exactly `limit` items → no next page ---
    @Test
    void fromOversizedList_whenSizeEqualsLimit_marksNoMore() {
        List<Item> input = List.of(item(5), item(4), item(3));

        Page<Item> page = Page.fromOversizedList(input, 3, CURSOR_OF);

        assertThat(page.items()).hasSize(3);
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    // --- P2: less than `limit` items → no next page ---
    @Test
    void fromOversizedList_whenSizeBelowLimit_marksNoMore() {
        List<Item> input = List.of(item(5), item(4));

        Page<Item> page = Page.fromOversizedList(input, 5, CURSOR_OF);

        assertThat(page.items()).hasSize(2);
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    // --- P3: limit + 1 items → drops the extra, cursor from the LAST RETURNED item ---
    @Test
    void fromOversizedList_whenSizeAboveLimit_dropsExtraAndCursorsLastReturned() {
        Item i5 = item(5), i4 = item(4), i3 = item(3), i2 = item(2);
        List<Item> input = List.of(i5, i4, i3, i2);

        Page<Item> page = Page.fromOversizedList(input, 3, CURSOR_OF);

        assertThat(page.items()).containsExactly(i5, i4, i3);
        assertThat(page.hasMore()).isTrue();
        assertThat(page.nextCursor()).isNotNull();
        // Cursor must point at the last returned item (i3), not the dropped one (i2).
        Cursor decoded = Cursor.decode(page.nextCursor());
        assertThat(decoded.createdAt()).isEqualTo(i3.createdAt());
        assertThat(decoded.id()).isEqualTo(i3.id());
    }

    // --- P4: empty list → empty page, not null cursor ---
    @Test
    void fromOversizedList_emptyInput_returnsEmptyPage() {
        Page<Item> page = Page.fromOversizedList(List.of(), 10, CURSOR_OF);

        assertThat(page.items()).isEmpty();
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    // --- P5: returned list is defensively copied (mutating input doesn't affect the page) ---
    @Test
    void fromOversizedList_defensiveCopiesInput() {
        List<Item> input = new java.util.ArrayList<>(List.of(item(5), item(4)));

        Page<Item> page = Page.fromOversizedList(input, 5, CURSOR_OF);
        input.clear();

        assertThat(page.items()).hasSize(2);
    }

    // --- P6: map preserves cursor and hasMore, transforms items ---
    @Test
    void map_preservesCursorAndHasMore_transformsItems() {
        Page<Item> source = new Page<>(List.of(item(2), item(1)), "v1.opaque", true);

        Page<String> mapped = source.map(Item::label);

        assertThat(mapped.items()).containsExactly("item-2", "item-1");
        assertThat(mapped.hasMore()).isTrue();
        assertThat(mapped.nextCursor()).isEqualTo("v1.opaque");
    }

    // --- P7: empty() is the canonical zero value ---
    @Test
    void empty_returnsEmptyPageWithoutCursor() {
        Page<Item> empty = Page.empty();

        assertThat(empty.items()).isEmpty();
        assertThat(empty.hasMore()).isFalse();
        assertThat(empty.nextCursor()).isNull();
    }

    // --- P8: realistic round-trip — 21 items, limit 20, cursor points at item 20 not 21 ---
    @Test
    void fromOversizedList_realisticLimit20WithLimitPlusOne_cursorPointsToLastReturned() {
        List<Item> input = IntStream.range(0, 21)
                .mapToObj(n -> item(100 - n))  // n=0 is newest; results in DESC order
                .toList();

        Page<Item> page = Page.fromOversizedList(input, 20, CURSOR_OF);

        assertThat(page.items()).hasSize(20);
        assertThat(page.items().get(19)).isEqualTo(input.get(19));
        assertThat(page.hasMore()).isTrue();
        Cursor decoded = Cursor.decode(page.nextCursor());
        assertThat(decoded.id()).isEqualTo(input.get(19).id());
    }
}
