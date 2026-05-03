package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.AiFeedback;
import com.orizon.coreapi.domain.model.AiFeedbackType;
import com.orizon.coreapi.domain.port.in.GetAiFeedbackUseCase;
import com.orizon.coreapi.domain.port.out.AiFeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiFeedbackServiceTest {

    @Mock AiFeedbackRepository aiFeedbackRepository;

    private AiFeedbackService service;

    @BeforeEach
    void setUp() {
        service = new AiFeedbackService(aiFeedbackRepository);
    }

    // --- A1 ---
    @Test
    void create_succeeds_initializesReadAsFalse() {
        UUID userId = UUID.randomUUID();
        AiFeedback saved = buildFeedback(UUID.randomUUID(), userId, false);
        when(aiFeedbackRepository.save(any())).thenReturn(saved);

        AiFeedback result = service.execute(userId, AiFeedbackType.MONTHLY_REPORT,
                "May Report", "You spent a lot.", "{}", "2026-05");

        assertThat(result.isRead()).isFalse();
        verify(aiFeedbackRepository).save(any(AiFeedback.class));
    }

    // --- A2 ---
    @Test
    void list_withMonth_filtersByMonth() {
        UUID userId = UUID.randomUUID();
        List<AiFeedback> expected = List.of(buildFeedback(UUID.randomUUID(), userId, false));
        when(aiFeedbackRepository.findByUserIdAndReferenceMonth(userId, "2026-05")).thenReturn(expected);

        List<AiFeedback> result = service.execute(userId, "2026-05");

        assertThat(result).isEqualTo(expected);
        verify(aiFeedbackRepository).findByUserIdAndReferenceMonth(userId, "2026-05");
        verify(aiFeedbackRepository, never()).findByUserId(any());
    }

    // --- A3 ---
    @Test
    void list_withoutMonth_returnsAll() {
        UUID userId = UUID.randomUUID();
        List<AiFeedback> expected = List.of(
                buildFeedback(UUID.randomUUID(), userId, false),
                buildFeedback(UUID.randomUUID(), userId, true));
        when(aiFeedbackRepository.findByUserId(userId)).thenReturn(expected);

        List<AiFeedback> result = service.execute(userId, (String) null);

        assertThat(result).isEqualTo(expected);
        verify(aiFeedbackRepository).findByUserId(userId);
        verify(aiFeedbackRepository, never()).findByUserIdAndReferenceMonth(any(), any());
    }

    // --- A4 ---
    @Test
    void get_throwsWhenOwnershipMismatch() {
        UUID feedbackId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        AiFeedback feedback = buildFeedback(feedbackId, realOwner, false);
        when(aiFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));

        GetAiFeedbackUseCase getUseCase = service;
        assertThatThrownBy(() -> getUseCase.execute(attacker, feedbackId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- A5 ---
    @Test
    void markAsRead_throwsWhenOwnershipMismatch() {
        UUID feedbackId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        AiFeedback feedback = buildFeedback(feedbackId, realOwner, false);
        when(aiFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));

        assertThatThrownBy(() -> service.execute(attacker, feedbackId, true))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(aiFeedbackRepository, never()).save(any());
    }

    // --- fixtures ---

    private AiFeedback buildFeedback(UUID id, UUID userId, boolean read) {
        AiFeedback f = new AiFeedback();
        f.setId(id);
        f.setUserId(userId);
        f.setType(AiFeedbackType.MONTHLY_REPORT);
        f.setTitle("Test Feedback");
        f.setContent("Some content.");
        f.setMetadata("{}");
        f.setReferenceMonth("2026-05");
        f.setRead(read);
        f.setCreatedAt(LocalDateTime.now());
        return f;
    }
}
