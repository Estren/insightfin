package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.domain.model.AiFeedback;
import com.orizon.coreapi.domain.model.AiFeedbackType;
import com.orizon.coreapi.domain.model.Budget;
import com.orizon.coreapi.domain.model.BudgetAlert;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.out.AiFeedbackRepository;
import com.orizon.coreapi.domain.port.out.BudgetAlertRepository;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class NotificationControllerIT {

    @Inject AiFeedbackRepository aiFeedbackRepository;
    @Inject BudgetAlertRepository budgetAlertRepository;
    @Inject BudgetRepository budgetRepository;
    @Inject CategoryRepository categoryRepository;

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

    // --- F1 ---
    @Test
    void list_withoutToken_returns401() {
        given().when().get("/api/notifications")
                .then().statusCode(401);
    }

    // --- F2 ---
    @Test
    void unreadCount_withoutToken_returns401() {
        given().when().get("/api/notifications/unread-count")
                .then().statusCode(401);
    }

    // --- F3 ---
    @Test
    void list_authenticatedNoNotifications_returnsEmptyEnvelope() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications")
                .then().statusCode(200)
                .body("items", hasSize(0))
                .body("hasMore", equalTo(false))
                .body("nextCursor", nullValue());
    }

    // --- F4 ---
    @Test
    void unreadCount_authenticatedNoNotifications_returnsZeros() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications/unread-count")
                .then().statusCode(200)
                .body("aiFeedbacks", equalTo(0))
                .body("budgetAlerts", equalTo(0))
                .body("total", equalTo(0));
    }

    // --- F5: mixed feed, sorted desc, category name resolved ---
    @Test
    void list_withMixedSources_returnsSortedFeedWithCategoryName() {
        String token = registerAndLogin(uniqueEmail());
        UUID userId = currentUserId(token);

        Category category = persistCategory(userId, "Food");
        Budget budget = persistBudget(userId, category.getId());

        LocalDateTime t0 = LocalDateTime.now().minusHours(2);
        LocalDateTime t1 = LocalDateTime.now().minusHours(1);
        LocalDateTime t2 = LocalDateTime.now();

        persistFeedback(userId, t1);
        persistAlert(userId, budget.getId(), 80, t0);
        persistAlert(userId, budget.getId(), 100, t2);

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications")
                .then().statusCode(200)
                .body("items", hasSize(3))
                .body("hasMore", equalTo(false))
                .body("nextCursor", nullValue())
                .body("items[0].kind", equalTo("BUDGET_ALERT"))
                .body("items[0].categoryName", equalTo("Food"))
                .body("items[0].thresholdPercentage", equalTo(100))
                .body("items[1].kind", equalTo("AI_FEEDBACK"))
                .body("items[1].title", equalTo("Monthly report"))
                .body("items[2].kind", equalTo("BUDGET_ALERT"))
                .body("items[2].thresholdPercentage", equalTo(80));
    }

    // --- F7: limit smaller than total → first page has cursor + hasMore=true ---
    @Test
    void list_withLimitSmallerThanTotal_returnsCursorAndHasMore() {
        String token = registerAndLogin(uniqueEmail());
        UUID userId = currentUserId(token);

        // 4 notifications spread out so the order is deterministic.
        LocalDateTime base = LocalDateTime.now().withNano(0);
        persistFeedback(userId, base.minusMinutes(1));
        persistFeedback(userId, base.minusMinutes(2));
        persistFeedback(userId, base.minusMinutes(3));
        persistFeedback(userId, base.minusMinutes(4));

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=2")
                .then().statusCode(200)
                .body("items", hasSize(2))
                .body("hasMore", equalTo(true))
                .body("nextCursor", notNullValue());
    }

    // --- F8: paging via cursor returns the remainder without overlap and ends with hasMore=false ---
    @Test
    void list_followingCursor_returnsRemainderWithoutOverlap() {
        String token = registerAndLogin(uniqueEmail());
        UUID userId = currentUserId(token);

        LocalDateTime base = LocalDateTime.now().withNano(0);
        persistFeedback(userId, base.minusMinutes(1));  // newest
        persistFeedback(userId, base.minusMinutes(2));
        persistFeedback(userId, base.minusMinutes(3));
        persistFeedback(userId, base.minusMinutes(4));  // oldest

        String firstId = given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=2")
                .then().statusCode(200)
                .extract().jsonPath().getString("items[0].id");
        String secondId = given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=2")
                .then().extract().jsonPath().getString("items[1].id");
        String cursor = given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=2")
                .then().extract().jsonPath().getString("nextCursor");

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=2&cursor=" + cursor)
                .then().statusCode(200)
                .body("items", hasSize(2))
                .body("hasMore", equalTo(false))
                .body("nextCursor", nullValue())
                .body("items[0].id", org.hamcrest.Matchers.not(equalTo(firstId)))
                .body("items[0].id", org.hamcrest.Matchers.not(equalTo(secondId)))
                .body("items[1].id", org.hamcrest.Matchers.not(equalTo(firstId)))
                .body("items[1].id", org.hamcrest.Matchers.not(equalTo(secondId)));
    }

    // --- F9: malformed cursor → 400 ---
    @Test
    void list_invalidCursor_returns400() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?cursor=garbage")
                .then().statusCode(400);
    }

    // --- F10: limit out of range → 400 (both ends) ---
    @Test
    void list_limitBelowMin_returns400() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=0")
                .then().statusCode(400);
    }

    @Test
    void list_limitAboveMax_returns400() {
        String token = registerAndLogin(uniqueEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications?limit=101")
                .then().statusCode(400);
    }

    // --- F6: unread count sums the two sources, ignores read items ---
    @Test
    void unreadCount_withMixedReadStates_countsOnlyUnread() {
        String token = registerAndLogin(uniqueEmail());
        UUID userId = currentUserId(token);

        persistFeedback(userId, LocalDateTime.now());                        // unread
        persistReadFeedback(userId, LocalDateTime.now().minusDays(1));       // read → ignored
        persistAlert(userId, UUID.randomUUID(), 50, LocalDateTime.now());    // unread
        persistAlert(userId, UUID.randomUUID(), 80, LocalDateTime.now());    // unread

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/notifications/unread-count")
                .then().statusCode(200)
                .body("aiFeedbacks", equalTo(1))
                .body("budgetAlerts", equalTo(2))
                .body("total", equalTo(3));
    }

    // ---------- helpers ----------

    private Category persistCategory(UUID userId, String name) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setUserId(userId);
        category.setName(name);
        category.setType(TransactionType.EXPENSE);
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    private Budget persistBudget(UUID userId, UUID categoryId) {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setUserId(userId);
        budget.setCategoryId(categoryId);
        budget.setAmount(new BigDecimal("500.00"));
        budget.setMonth("2026-05");
        budget.setCreatedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    private void persistFeedback(UUID userId, LocalDateTime createdAt) {
        aiFeedbackRepository.save(new AiFeedback(
                UUID.randomUUID(), userId, AiFeedbackType.MONTHLY_REPORT,
                "Monthly report", "You spent 30% more on food",
                null, "2026-05", false, createdAt));
    }

    private void persistReadFeedback(UUID userId, LocalDateTime createdAt) {
        aiFeedbackRepository.save(new AiFeedback(
                UUID.randomUUID(), userId, AiFeedbackType.MONTHLY_REPORT,
                "Old report", "...", null, "2026-04", true, createdAt));
    }

    private void persistAlert(UUID userId, UUID budgetId, int threshold, LocalDateTime createdAt) {
        budgetAlertRepository.save(new BudgetAlert(UUID.randomUUID(), userId, budgetId,
                threshold, new BigDecimal("400.00"), new BigDecimal("500.00"),
                createdAt, false, createdAt));
    }
}
