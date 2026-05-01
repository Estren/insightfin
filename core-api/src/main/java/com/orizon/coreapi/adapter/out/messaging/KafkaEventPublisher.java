package com.orizon.coreapi.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orizon.coreapi.domain.event.GoalContributedEvent;
import com.orizon.coreapi.domain.event.TransactionCreatedEvent;
import com.orizon.coreapi.domain.port.out.EventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = Logger.getLogger(KafkaEventPublisher.class);

    @Inject
    @Channel("transaction-created")
    Emitter<String> transactionEmitter;

    @Inject
    @Channel("goal-contributed")
    Emitter<String> goalEmitter;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void publishTransactionCreated(TransactionCreatedEvent event) {
        publish(transactionEmitter, event, "transaction.created");
    }

    @Override
    public void publishGoalContributed(GoalContributedEvent event) {
        publish(goalEmitter, event, "goal.contributed");
    }

    private void publish(Emitter<String> emitter, Object event, String topic) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            emitter.send(payload).exceptionally(err -> {
                log.errorf("Failed to publish to %s: %s", topic, err.getMessage());
                return null;
            });
        } catch (JsonProcessingException e) {
            log.errorf("Failed to serialize event for topic %s: %s", topic, e.getMessage());
        }
    }
}
