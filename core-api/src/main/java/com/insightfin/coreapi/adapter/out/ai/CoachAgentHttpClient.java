package com.insightfin.coreapi.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightfin.coreapi.domain.model.CoachMessage;
import com.insightfin.coreapi.domain.port.out.CoachAgentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP adapter to the AI service for non-streaming Coach operations.
 *
 * Uses the JDK HttpClient forced to HTTP/1.1 — uvicorn (the FastAPI server)
 * is HTTP/1.1 only, and the default HTTP/2-then-downgrade negotiation has
 * been observed to drop request bodies against it.
 */
@ApplicationScoped
public class CoachAgentHttpClient implements CoachAgentGateway {

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "ai.service.url", defaultValue = "http://ai:8081")
    String aiServiceUrl;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @Override
    public String createThread() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + "/coach/threads"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(
                        "AI service createThread failed: HTTP " + response.statusCode() + " " + response.body());
            }
            JsonNode node = objectMapper.readTree(response.body());
            return node.get("threadId").asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("AI service createThread error", e);
        }
    }

    @Override
    public List<CoachMessage> listMessages(String foundryThreadId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + "/coach/threads/" + foundryThreadId + "/messages"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(
                        "AI service listMessages failed: HTTP " + response.statusCode() + " " + response.body());
            }
            return parseMessages(response.body());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("AI service listMessages error", e);
        }
    }

    private List<CoachMessage> parseMessages(String body) throws Exception {
        JsonNode array = objectMapper.readTree(body);
        List<CoachMessage> messages = new ArrayList<>();
        for (JsonNode node : array) {
            List<CoachMessage.Citation> citations = new ArrayList<>();
            JsonNode citationsNode = node.get("citations");
            if (citationsNode != null && citationsNode.isArray()) {
                for (JsonNode c : citationsNode) {
                    citations.add(new CoachMessage.Citation(
                            c.path("marker").asInt(),
                            c.path("filename").asText()));
                }
            }
            LocalDateTime createdAt = node.hasNonNull("createdAt")
                    ? LocalDateTime.parse(node.get("createdAt").asText())
                    : null;
            messages.add(new CoachMessage(
                    node.path("role").asText(),
                    node.path("text").asText(),
                    citations,
                    createdAt));
        }
        return messages;
    }
}
