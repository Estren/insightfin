package com.insightfin.coreapi.domain.model;

public record UnreadCounts(int aiFeedbacks, int budgetAlerts) {

    public int total() {
        return aiFeedbacks + budgetAlerts;
    }
}
