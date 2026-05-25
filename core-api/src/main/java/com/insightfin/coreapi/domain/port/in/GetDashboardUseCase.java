package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.DashboardSummary;

import java.util.UUID;

public interface GetDashboardUseCase {
    DashboardSummary execute(UUID userId, String month);
}
