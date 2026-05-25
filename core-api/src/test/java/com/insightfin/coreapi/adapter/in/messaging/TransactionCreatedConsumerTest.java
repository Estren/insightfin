package com.insightfin.coreapi.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insightfin.coreapi.domain.event.TransactionCreatedEvent;
import com.insightfin.coreapi.domain.port.in.EvaluateBudgetAlertsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCreatedConsumerTest {

    @Mock EvaluateBudgetAlertsUseCase evaluateBudgetAlertsUseCase;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    TransactionCreatedConsumer consumer;

    private UUID userId;
    private UUID categoryId;
    private UUID transactionId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        date = LocalDate.of(2026, 5, 23);
    }

    // --- C1: expense event triggers the use case with the right args ---
    @Test
    void onTransactionCreated_expense_triggersEvaluation() throws Exception {
        String payload = serialize(new TransactionCreatedEvent(
                userId, transactionId, categoryId, new BigDecimal("100.00"), "EXPENSE", date));

        consumer.onTransactionCreated(payload);

        verify(evaluateBudgetAlertsUseCase).execute(userId, categoryId, date);
    }

    // --- C2: income event is ignored ---
    @Test
    void onTransactionCreated_income_isIgnored() throws Exception {
        String payload = serialize(new TransactionCreatedEvent(
                userId, transactionId, categoryId, new BigDecimal("100.00"), "INCOME", date));

        consumer.onTransactionCreated(payload);

        verify(evaluateBudgetAlertsUseCase, never()).execute(any(), any(), any());
    }

    // --- C3: malformed payload is swallowed (no rethrow that would poison the consumer) ---
    @Test
    void onTransactionCreated_malformedPayload_swallowsError() {
        consumer.onTransactionCreated("{ not valid json");

        verifyNoInteractions(evaluateBudgetAlertsUseCase);
    }

    // --- C4: downstream failure is swallowed ---
    @Test
    void onTransactionCreated_whenUseCaseThrows_swallowsError() throws Exception {
        String payload = serialize(new TransactionCreatedEvent(
                userId, transactionId, categoryId, new BigDecimal("100.00"), "EXPENSE", date));
        when(evaluateBudgetAlertsUseCase.execute(userId, categoryId, date))
                .thenThrow(new RuntimeException("DB down"));

        consumer.onTransactionCreated(payload);  // must not throw
    }

    private String serialize(TransactionCreatedEvent event) throws Exception {
        return objectMapper.writeValueAsString(event);
    }
}
