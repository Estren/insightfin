package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.CoachMessage;

import java.util.List;

/**
 * Outbound port to the AI service for Coach thread operations that aren't
 * streaming (thread creation and history hydration). The streaming chat
 * proxy stays in CoachController because it pipes raw SSE bytes.
 */
public interface CoachAgentGateway {

    /** Create an empty Foundry thread and return its id. */
    String createThread();

    /** Fetch the full message history of a Foundry thread. */
    List<CoachMessage> listMessages(String foundryThreadId);
}
