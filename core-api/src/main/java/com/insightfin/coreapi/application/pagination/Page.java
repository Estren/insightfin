package com.insightfin.coreapi.application.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * A page of items from a cursor-paginated endpoint.
 *
 * <p>{@code nextCursor} is {@code null} when {@code hasMore} is {@code false}.
 * Clients should treat {@code hasMore} as the source of truth for "load more"
 * decisions — {@code nextCursor} being non-null while {@code hasMore} is false
 * is a server bug, not a valid state.
 */
public record Page<T>(List<T> items, String nextCursor, boolean hasMore) {

    private static final Page<?> EMPTY = new Page<>(List.of(), null, false);

    @SuppressWarnings("unchecked")
    public static <T> Page<T> empty() {
        return (Page<T>) EMPTY;
    }

    /**
     * Builds a {@code Page} from a list that may contain up to {@code limit + 1}
     * items (the standard "fetch one extra to detect hasMore" trick).
     *
     * <p>If {@code oversized.size() > limit}, the extra trailing item is
     * dropped and {@code hasMore} is set to {@code true}; the cursor is derived
     * from the last item that's actually returned (not the dropped one).
     *
     * @param oversized   items as fetched, with at most {@code limit + 1} entries
     * @param limit       requested page size (the contract returned to the client)
     * @param cursorOf    derives a {@link Cursor} from an item — usually
     *                    {@code item -> new Cursor(item.createdAt(), item.id())}
     */
    public static <T> Page<T> fromOversizedList(List<T> oversized, int limit, Function<T, Cursor> cursorOf) {
        if (oversized.size() > limit) {
            List<T> page = List.copyOf(oversized.subList(0, limit));
            String cursor = cursorOf.apply(page.get(page.size() - 1)).encode();
            return new Page<>(page, cursor, true);
        }
        return new Page<>(List.copyOf(oversized), null, false);
    }

    /** Transforms each item while preserving {@code nextCursor} and {@code hasMore}. */
    public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = items.stream().<R>map(mapper).toList();
        return new Page<>(mapped, nextCursor, hasMore);
    }
}
