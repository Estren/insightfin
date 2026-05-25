package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.domain.model.AiFeedback;
import com.insightfin.coreapi.domain.model.AiFeedbackType;
import com.insightfin.coreapi.domain.port.out.AiFeedbackRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class AiFeedbackControllerIT {

    @Inject
    AiFeedbackRepository aiFeedbackRepository;

    private static String uniqueEmail() {
        return "u-" + UUID.randomUUID() + "@test.com";
    }

    private static String registerAndLogin(String email) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Test","email":"%s","password":"password123"}
                        """.formatted(email))
                .when().post("/api/auth/register")
                .then().statusCode(201);

        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"password123"}
                        """.formatted(email))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("accessToken");
    }

    private static UUID currentUserId(String token) {
        return UUID.fromString(given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/users/me")
                .then().statusCode(200)
                .extract().jsonPath().getString("id"));
    }

    private AiFeedback persistFeedback(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        AiFeedback feedback = new AiFeedback(UUID.randomUUID(), userId, AiFeedbackType.MONTHLY_REPORT,
                "Monthly report", "You spent 30% more on food.",
                null, "2026-05", false, now);
        return aiFeedbackRepository.save(feedback);
    }

    // --- AF1 ---
    @Test
    void markAsRead_withoutToken_returns401() {
        given().when().patch("/api/feedbacks/{id}/read", UUID.randomUUID())
                .then().statusCode(401);
    }

    // --- AF2 ---
    @Test
    void markAsRead_unknownFeedback_returns404() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().patch("/api/feedbacks/{id}/read", UUID.randomUUID())
                .then().statusCode(404);
    }

    // --- AF3: regression — markAsRead used to 500 because the adapter used
    // persist() (INSERT-only) on an already-managed entity. This locks in merge(). ---
    @Test
    void markAsRead_unreadFeedback_returns204AndPersistsReadFlag() {
        String token = registerAndLogin(uniqueEmail());
        UUID userId = currentUserId(token);
        AiFeedback feedback = persistFeedback(userId);

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/feedbacks?month=2026-05")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].read", equalTo(false));

        given()
                .header("Authorization", "Bearer " + token)
                .when().patch("/api/feedbacks/{id}/read", feedback.getId())
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/feedbacks?month=2026-05")
                .then().statusCode(200)
                .body("[0].read", equalTo(true));
    }

    // --- AF4: cross-user PATCH returns 404 (avoids leaking existence) ---
    @Test
    void markAsRead_anotherUsersFeedback_returns404() {
        String victimToken = registerAndLogin(uniqueEmail());
        UUID victimId = currentUserId(victimToken);
        AiFeedback victimFeedback = persistFeedback(victimId);

        String attackerToken = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + attackerToken)
                .when().patch("/api/feedbacks/{id}/read", victimFeedback.getId())
                .then().statusCode(404);
    }
}
