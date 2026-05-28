package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.CoachMessage;
import com.insightfin.coreapi.domain.model.CoachThread;
import com.insightfin.coreapi.domain.port.in.CoachThreadUseCases;
import com.insightfin.coreapi.domain.port.out.CoachAgentGateway;
import com.insightfin.coreapi.domain.port.out.CoachThreadRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CoachThreadService implements CoachThreadUseCases {

    private static final int TITLE_MAX_LENGTH = 60;

    private final CoachThreadRepository repository;
    private final CoachAgentGateway agentGateway;

    public CoachThreadService(CoachThreadRepository repository, CoachAgentGateway agentGateway) {
        this.repository = repository;
        this.agentGateway = agentGateway;
    }

    @Override
    public CoachThread create(UUID userId, String firstMessage) {
        String foundryThreadId = agentGateway.createThread();
        LocalDateTime now = LocalDateTime.now();
        CoachThread thread = new CoachThread(
                UUID.randomUUID(),
                userId,
                foundryThreadId,
                titleFrom(firstMessage),
                now,
                now);
        return repository.save(thread);
    }

    @Override
    public List<CoachThread> list(UUID userId) {
        return repository.findByUserIdOrderByLastMessageDesc(userId);
    }

    @Override
    public CoachThread get(UUID threadId, UUID userId) {
        return requireOwned(threadId, userId);
    }

    @Override
    public List<CoachMessage> getMessages(UUID threadId, UUID userId) {
        CoachThread thread = requireOwned(threadId, userId);
        return agentGateway.listMessages(thread.getFoundryThreadId());
    }

    @Override
    public CoachThread rename(UUID threadId, UUID userId, String title) {
        CoachThread thread = requireOwned(threadId, userId);
        thread.setTitle(truncate(title));
        return repository.save(thread);
    }

    @Override
    public void delete(UUID threadId, UUID userId) {
        CoachThread thread = requireOwned(threadId, userId);
        repository.deleteById(thread.getId());
    }

    @Override
    public void touch(UUID threadId, UUID userId) {
        CoachThread thread = requireOwned(threadId, userId);
        thread.setLastMessageAt(LocalDateTime.now());
        repository.save(thread);
    }

    private CoachThread requireOwned(UUID threadId, UUID userId) {
        CoachThread thread = repository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachThread", threadId));
        if (!thread.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("CoachThread", threadId);
        }
        return thread;
    }

    private String titleFrom(String firstMessage) {
        if (firstMessage == null || firstMessage.isBlank()) {
            return "Nova conversa";
        }
        return truncate(firstMessage.strip());
    }

    private String truncate(String text) {
        String clean = text == null ? "" : text.strip();
        return clean.length() <= TITLE_MAX_LENGTH
                ? clean
                : clean.substring(0, TITLE_MAX_LENGTH).strip() + "…";
    }
}
