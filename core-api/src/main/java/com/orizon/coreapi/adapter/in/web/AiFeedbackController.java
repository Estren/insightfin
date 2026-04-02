package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.AiFeedbackResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateAiFeedbackRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.CreateAiFeedbackUseCase;
import com.orizon.coreapi.domain.port.in.GetAiFeedbackUseCase;
import com.orizon.coreapi.domain.port.in.ListAiFeedbacksUseCase;
import com.orizon.coreapi.domain.port.in.MarkFeedbackAsReadUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/feedbacks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiFeedbackController {

    @Inject
    CreateAiFeedbackUseCase createAiFeedbackUseCase;

    @Inject
    ListAiFeedbacksUseCase listAiFeedbacksUseCase;

    @Inject
    GetAiFeedbackUseCase getAiFeedbackUseCase;

    @Inject
    MarkFeedbackAsReadUseCase markFeedbackAsReadUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateAiFeedbackRequest request) {
        var feedback = createAiFeedbackUseCase.execute(
                request.userId(), request.type(), request.title(),
                request.content(), request.metadata(), request.referenceMonth());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(feedback)).build();
    }

    @GET
    public List<AiFeedbackResponse> list(@QueryParam("month") String referenceMonth) {
        return listAiFeedbacksUseCase.execute(authenticatedUser.getUserId(), referenceMonth)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
    public AiFeedbackResponse get(@PathParam("id") UUID id) {
        var feedback = getAiFeedbackUseCase.execute(authenticatedUser.getUserId(), id);
        return WebMapper.toResponse(feedback);
    }

    @PATCH
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") UUID id) {
        markFeedbackAsReadUseCase.execute(authenticatedUser.getUserId(), id, true);
        return Response.noContent().build();
    }
}
