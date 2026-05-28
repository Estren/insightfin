package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.CoachMessageResponse;
import com.insightfin.coreapi.adapter.in.web.dto.CoachThreadResponse;
import com.insightfin.coreapi.adapter.in.web.dto.CreateCoachThreadRequest;
import com.insightfin.coreapi.adapter.in.web.dto.RenameCoachThreadRequest;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.model.CoachMessage;
import com.insightfin.coreapi.domain.model.CoachThread;
import com.insightfin.coreapi.domain.port.in.CoachThreadUseCases;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * CRUD for Coach conversations (the ChatGPT-style sidebar). The streaming
 * chat itself stays in {@link CoachController}. All routes require a valid
 * JWT (enforced by JwtAuthenticationFilter) and operate on the authenticated
 * user's own threads.
 */
@Path("/api/coach/threads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoachThreadController {

    @Inject
    CoachThreadUseCases coachThreads;

    @Inject
    AuthenticatedUser authenticatedUser;

    @GET
    public List<CoachThreadResponse> list() {
        return coachThreads.list(authenticatedUser.getUserId())
                .stream()
                .map(CoachThreadController::toResponse)
                .toList();
    }

    @POST
    public Response create(@Valid CreateCoachThreadRequest request) {
        CoachThread thread = coachThreads.create(authenticatedUser.getUserId(), request.firstMessage());
        return Response.status(Response.Status.CREATED).entity(toResponse(thread)).build();
    }

    @GET
    @Path("/{id}/messages")
    public List<CoachMessageResponse> messages(@PathParam("id") UUID id) {
        return coachThreads.getMessages(id, authenticatedUser.getUserId())
                .stream()
                .map(CoachThreadController::toResponse)
                .toList();
    }

    @PATCH
    @Path("/{id}")
    public CoachThreadResponse rename(@PathParam("id") UUID id, @Valid RenameCoachThreadRequest request) {
        return toResponse(coachThreads.rename(id, authenticatedUser.getUserId(), request.title()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        coachThreads.delete(id, authenticatedUser.getUserId());
        return Response.noContent().build();
    }

    private static CoachThreadResponse toResponse(CoachThread thread) {
        return new CoachThreadResponse(
                thread.getId(), thread.getTitle(), thread.getCreatedAt(), thread.getLastMessageAt());
    }

    private static CoachMessageResponse toResponse(CoachMessage message) {
        List<CoachMessageResponse.CitationResponse> citations = message.citations().stream()
                .map(c -> new CoachMessageResponse.CitationResponse(c.marker(), c.filename()))
                .toList();
        return new CoachMessageResponse(message.role(), message.text(), citations, message.createdAt());
    }
}
