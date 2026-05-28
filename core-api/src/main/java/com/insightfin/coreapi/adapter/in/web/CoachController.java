package com.insightfin.coreapi.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightfin.coreapi.adapter.in.web.dto.CoachChatRequest;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.model.CoachThread;
import com.insightfin.coreapi.domain.port.in.CoachThreadUseCases;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Proxies the authenticated frontend's chat request to the internal AI service
 * and streams Server-Sent Events back unmodified.
 *
 * <p>The frontend never sees the AI service directly: this controller validates
 * the JWT (via {@link com.insightfin.coreapi.config.security.JwtAuthenticationFilter}),
 * extracts the user id, and only then opens the upstream call. The AI service is
 * not exposed publicly in production — internal Container App ingress only.</p>
 *
 * <p>Implementation note: Quarkus REST consumers of upstream SSE either parse
 * each event into a typed shape (losing the {@code event:} name) or require a
 * Mutiny pipeline that's overkill for a passthrough. The simpler path is the
 * JDK {@link HttpClient} with {@code BodyHandlers.ofInputStream()}: send blocks
 * until response headers arrive, then the body streams chunk by chunk. We copy
 * those chunks into the {@link StreamingOutput} of our response, preserving the
 * SSE wire format verbatim.</p>
 */
@Path("/api/coach")
@Produces(MediaType.SERVER_SENT_EVENTS)
@Consumes(MediaType.APPLICATION_JSON)
public class CoachController {

    @Inject
    AuthenticatedUser authenticatedUser;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CoachThreadUseCases coachThreads;

    @ConfigProperty(name = "ai.service.url", defaultValue = "http://ai:8081")
    String aiServiceUrl;

    private static final Logger LOG = Logger.getLogger(CoachController.class);

    // Force HTTP/1.1 — uvicorn (FastAPI) is HTTP/1.1 only; the JDK client
    // defaults to HTTP/2 and the downgrade negotiation has been observed to
    // drop the request body when the upstream advertises HTTP/1.1.
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @POST
    @Path("/chat")
    public Response chat(@Valid CoachChatRequest body) throws Exception {
        UUID userId = authenticatedUser.getUserId();

        if (body.threadId() == null || body.threadId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"detail\":\"threadId is required — create a thread first via POST /api/coach/threads\"}")
                    .build();
        }

        UUID threadId;
        try {
            threadId = UUID.fromString(body.threadId());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"detail\":\"threadId is not a valid UUID\"}")
                    .build();
        }

        // Resolve our thread id to the Foundry thread id, enforcing ownership
        // (throws 404 if the thread doesn't belong to this user).
        CoachThread thread = coachThreads.get(threadId, userId);
        coachThreads.touch(threadId, userId);

        Map<String, Object> upstreamBody = new HashMap<>();
        upstreamBody.put("userId", userId.toString());
        upstreamBody.put("question", body.question());
        upstreamBody.put("threadId", thread.getFoundryThreadId());
        String payload = objectMapper.writeValueAsString(upstreamBody);

        LOG.infof("coach_chat_proxy upstream=%s userId=%s threadId=%s questionLength=%d",
                aiServiceUrl, userId, threadId, body.question().length());

        HttpRequest upstream = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + "/coach/chat/stream"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<java.io.InputStream> response = HTTP.send(
                upstream, HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() >= 400) {
            byte[] errorBody = response.body().readAllBytes();
            return Response.status(response.statusCode())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new String(errorBody))
                    .build();
        }

        StreamingOutput stream = output -> {
            try (var in = response.body()) {
                byte[] buffer = new byte[2048];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                    output.flush();
                }
            }
        };

        return Response.ok(stream)
                .type(MediaType.SERVER_SENT_EVENTS)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .build();
    }
}
