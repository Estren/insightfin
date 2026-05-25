package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.AiFeedback;
import com.insightfin.coreapi.domain.model.AiFeedbackType;
import com.insightfin.coreapi.domain.port.in.CreateAiFeedbackUseCase;
import com.insightfin.coreapi.domain.port.in.GetAiFeedbackUseCase;
import com.insightfin.coreapi.domain.port.in.ListAiFeedbacksUseCase;
import com.insightfin.coreapi.domain.port.in.MarkFeedbackAsReadUseCase;
import com.insightfin.coreapi.domain.port.out.AiFeedbackRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AiFeedbackService implements CreateAiFeedbackUseCase, ListAiFeedbacksUseCase,
        GetAiFeedbackUseCase, MarkFeedbackAsReadUseCase {

    private final AiFeedbackRepository aiFeedbackRepository;

    public AiFeedbackService(AiFeedbackRepository aiFeedbackRepository) {
        this.aiFeedbackRepository = aiFeedbackRepository;
    }

    @Override
    public AiFeedback execute(UUID userId, AiFeedbackType type, String title, String content,
                              String metadata, String referenceMonth) {
        AiFeedback feedback = new AiFeedback();
        feedback.setId(UUID.randomUUID());
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setMetadata(metadata);
        feedback.setReferenceMonth(referenceMonth);
        feedback.setRead(false);
        feedback.setCreatedAt(LocalDateTime.now());

        return aiFeedbackRepository.save(feedback);
    }

    @Override
    public List<AiFeedback> execute(UUID userId, String referenceMonth) {
        if (referenceMonth != null) {
            return aiFeedbackRepository.findByUserIdAndReferenceMonth(userId, referenceMonth);
        }
        return aiFeedbackRepository.findByUserId(userId);
    }

    @Override
    public AiFeedback execute(UUID userId, UUID feedbackId) {
        AiFeedback feedback = aiFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("AiFeedback", feedbackId));

        if (!feedback.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("AiFeedback", feedbackId);
        }

        return feedback;
    }

    @Override
    public void execute(UUID userId, UUID feedbackId, boolean markAsRead) {
        AiFeedback feedback = aiFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("AiFeedback", feedbackId));

        if (!feedback.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("AiFeedback", feedbackId);
        }

        feedback.setRead(true);
        aiFeedbackRepository.save(feedback);
    }
}
