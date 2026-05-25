package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.event.GoalContributedEvent;
import com.insightfin.coreapi.domain.event.TransactionCreatedEvent;

public interface EventPublisher {
    void publishTransactionCreated(TransactionCreatedEvent event);
    void publishGoalContributed(GoalContributedEvent event);
}
