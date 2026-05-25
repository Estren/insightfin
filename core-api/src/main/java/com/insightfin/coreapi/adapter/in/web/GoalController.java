package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.*;
import com.insightfin.coreapi.adapter.in.web.mapper.WebMapper;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.port.in.ContributeToGoalUseCase;
import com.insightfin.coreapi.domain.port.in.CreateGoalUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteGoalUseCase;
import com.insightfin.coreapi.domain.port.in.ListGoalsUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateGoalUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoalController {

    @Inject
    CreateGoalUseCase createGoalUseCase;

    @Inject
    ListGoalsUseCase listGoalsUseCase;

    @Inject
    ContributeToGoalUseCase contributeToGoalUseCase;

    @Inject
    UpdateGoalUseCase updateGoalUseCase;

    @Inject
    DeleteGoalUseCase deleteGoalUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateGoalRequest request) {
        var goal = createGoalUseCase.execute(
                authenticatedUser.getUserId(), request.title(), request.targetAmount(), request.deadline());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(goal)).build();
    }

    @GET
    public List<GoalResponse> list() {
        return listGoalsUseCase.execute(authenticatedUser.getUserId())
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @POST
    @Path("/{goalId}/contributions")
    public Response contribute(@PathParam("goalId") UUID goalId,
                               @Valid CreateGoalContributionRequest request) {
        var contribution = contributeToGoalUseCase.execute(goalId, request.amount(), request.date());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(contribution)).build();
    }

    @PUT
    @Path("/{id}")
    public GoalResponse update(@PathParam("id") UUID id, @Valid UpdateGoalRequest request) {
        var goal = updateGoalUseCase.execute(
                authenticatedUser.getUserId(), id, request.title(), request.targetAmount(), request.deadline());
        return WebMapper.toResponse(goal);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteGoalUseCase.execute(authenticatedUser.getUserId(), id);
        return Response.noContent().build();
    }
}
