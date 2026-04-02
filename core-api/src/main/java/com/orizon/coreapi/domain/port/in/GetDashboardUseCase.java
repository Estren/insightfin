package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.DashboardSummary;

import java.util.UUID;

public interface GetDashboardUseCase {
    DashboardSummary execute(UUID userId, String month);
}
