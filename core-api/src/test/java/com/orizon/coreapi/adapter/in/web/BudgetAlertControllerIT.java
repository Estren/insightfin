package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.domain.model.BudgetAlert;
import com.orizon.coreapi.domain.port.out.BudgetAlertRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class BudgetAlertControllerIT {

    @Inject
    BudgetAlertRepository alertRepository;

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

    private BudgetAlert persistAlert(UUID userId, int threshold) {
        LocalDateTime now = LocalDateTime.now();
        BudgetAlert alert = new BudgetAlert(UUID.randomUUID(), userId, UUID.randomUUID(),
                threshold, new BigDecimal("100.00"), new BigDecimal("200.00"),
                now, false, now);
        return alertRepository.save(alert);
    }

    // --- E1 ---
    @Test
    void list_withoutToken_returns401() {
        given().when().get("/api/budget-alerts")
                .then().statusCode(401);
    }

    // --- E2 ---
    @Test
    void list_authenticatedNoAlerts_returnsEmptyArray() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/budget-alerts")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    // --- E3 ---
    @Test
    void markAsRead_unknownAlert_returns404() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().patch("/api/budget-alerts/{id}/read", UUID.randomUUID())
                .then().statusCode(404);
    }

    // --- E4: happy path — persist alert via repo, GET sees it, PATCH marks it read ---
    @Test
    void list_thenMarkAsRead_reflectsReadStateOnSubsequentGet() {
        String token = registerAndLogin(uniqueEmail());
        UUID userId = currentUserId(token);
        BudgetAlert alert = persistAlert(userId, 80);

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/budget-alerts")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(alert.getId().toString()))
                .body("[0].thresholdPercentage", equalTo(80))
                .body("[0].read", equalTo(false));

        given()
                .header("Authorization", "Bearer " + token)
                .when().patch("/api/budget-alerts/{id}/read", alert.getId())
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/budget-alerts")
                .then().statusCode(200)
                .body("[0].read", equalTo(true));
    }

    // --- E5: a user cannot mark another user's alert as read (404, not 403, to avoid leaking existence) ---
    @Test
    void markAsRead_anotherUsersAlert_returns404() {
        String victimToken = registerAndLogin(uniqueEmail());
        UUID victimId = currentUserId(victimToken);
        BudgetAlert victimAlert = persistAlert(victimId, 50);

        String attackerToken = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + attackerToken)
                .when().patch("/api/budget-alerts/{id}/read", victimAlert.getId())
                .then().statusCode(404);
    }
}
