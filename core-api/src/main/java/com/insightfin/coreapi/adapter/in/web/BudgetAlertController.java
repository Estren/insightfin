package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.BudgetAlertResponse;
import com.insightfin.coreapi.adapter.in.web.mapper.WebMapper;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.port.in.ListBudgetAlertsUseCase;
import com.insightfin.coreapi.domain.port.in.MarkBudgetAlertAsReadUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/budget-alerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BudgetAlertController {

    @Inject
    ListBudgetAlertsUseCase listBudgetAlertsUseCase;

    @Inject
    MarkBudgetAlertAsReadUseCase markBudgetAlertAsReadUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @GET
    public List<BudgetAlertResponse> list() {
        return listBudgetAlertsUseCase.execute(authenticatedUser.getUserId())
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @PATCH
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") UUID id) {
        markBudgetAlertAsReadUseCase.execute(authenticatedUser.getUserId(), id);
        return Response.noContent().build();
    }
}
