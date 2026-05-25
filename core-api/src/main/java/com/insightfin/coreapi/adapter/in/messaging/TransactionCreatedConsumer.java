package com.insightfin.coreapi.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightfin.coreapi.domain.event.TransactionCreatedEvent;
import com.insightfin.coreapi.domain.port.in.EvaluateBudgetAlertsUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumes the {@code transaction.created} Kafka topic and asks the budget alert
 * service to evaluate whether any threshold (50/80/100%) was crossed for that
 * transaction's category in the same month.
 *
 * <p>Errors are swallowed and logged: the transaction is already durable in the
 * DB and alert generation is best-effort. Re-throwing would cause the consumer
 * to retry indefinitely on a poison-pill payload.
 */
@ApplicationScoped
public class TransactionCreatedConsumer {

    private static final Logger log = Logger.getLogger(TransactionCreatedConsumer.class);
    private static final String EXPENSE_TYPE = "EXPENSE";

    @Inject
    EvaluateBudgetAlertsUseCase evaluateBudgetAlertsUseCase;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("transaction-created-in")
    public void onTransactionCreated(String payload) {
        try {
            TransactionCreatedEvent event = objectMapper.readValue(payload, TransactionCreatedEvent.class);
            // INCOME transactions never consume a budget, so they cannot trigger a budget alert.
            if (!EXPENSE_TYPE.equals(event.type())) {
                return;
            }
            evaluateBudgetAlertsUseCase.execute(event.userId(), event.categoryId(), event.date());
        } catch (Exception e) {
            log.errorf("Failed to evaluate budget alerts for payload %s: %s", payload, e.getMessage());
        }
    }
}
