package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.BudgetResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateBudgetRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.CreateBudgetUseCase;
import com.orizon.coreapi.domain.port.in.ListBudgetsUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/budgets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BudgetController {

    @Inject
    CreateBudgetUseCase createBudgetUseCase;

    @Inject
    ListBudgetsUseCase listBudgetsUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateBudgetRequest request) {
        var budget = createBudgetUseCase.execute(
                authenticatedUser.getUserId(), request.categoryId(), request.amount(), request.month());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(budget)).build();
    }

    @GET
    public List<BudgetResponse> list(@QueryParam("month") String month) {
        return listBudgetsUseCase.execute(authenticatedUser.getUserId(), month)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
