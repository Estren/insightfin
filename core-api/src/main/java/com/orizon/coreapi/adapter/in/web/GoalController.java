package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.*;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.ContributeToGoalUseCase;
import com.orizon.coreapi.domain.port.in.CreateGoalUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoalController {

    @Inject
    CreateGoalUseCase createGoalUseCase;

    @Inject
    ContributeToGoalUseCase contributeToGoalUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateGoalRequest request) {
        var goal = createGoalUseCase.execute(
                authenticatedUser.getUserId(), request.title(), request.targetAmount(), request.deadline());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(goal)).build();
    }

    @POST
    @Path("/{goalId}/contributions")
    public Response contribute(@PathParam("goalId") UUID goalId,
                               @Valid CreateGoalContributionRequest request) {
        var contribution = contributeToGoalUseCase.execute(goalId, request.amount(), request.date());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(contribution)).build();
    }
}
