package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.CoachMessage;
import com.insightfin.coreapi.domain.model.CoachThread;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for managing Coach conversations. A single cohesive interface
 * (rather than one `execute` per operation) because several operations share
 * the {@code (UUID threadId, UUID userId)} signature, which would collide on
 * type erasure if expressed as separate single-method interfaces.
 *
 * Every method that takes both ids enforces that the thread belongs to the
 * user — callers pass the authenticated user id and a 404 is raised on
 * mismatch, so there's no cross-user access.
 */
public interface CoachThreadUseCases {

    /** Open a new Foundry thread and persist its metadata, titled from the first message. */
    CoachThread create(UUID userId, String firstMessage);

    /** List the user's threads, most recently active first. */
    List<CoachThread> list(UUID userId);

    /** Fetch a single thread owned by the user (used to resolve the Foundry id). */
    CoachThread get(UUID threadId, UUID userId);

    /** Hydrate the message history of a thread from Foundry. */
    List<CoachMessage> getMessages(UUID threadId, UUID userId);

    /** Rename a thread. */
    CoachThread rename(UUID threadId, UUID userId, String title);

    /** Delete a thread (hard delete). */
    void delete(UUID threadId, UUID userId);

    /** Bump last_message_at to now so the thread floats to the top of the list. */
    void touch(UUID threadId, UUID userId);
}
