package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.event.GoalContributedEvent;
import com.orizon.coreapi.domain.event.TransactionCreatedEvent;

public interface EventPublisher {
    void publishTransactionCreated(TransactionCreatedEvent event);
    void publishGoalContributed(GoalContributedEvent event);
}
