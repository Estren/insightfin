package com.insightfin.coreapi.adapter.in.web;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TransactionControllerIT {

    private String token;
    private String categoryId;

    @BeforeEach
    void setUp() {
        String email = "txn-" + UUID.randomUUID() + "@test.com";

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Test User","email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        categoryId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"name":"Food","type":"EXPENSE","icon":"food","color":"#FF0000"}
                        """)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .extract().jsonPath().getString("id");
    }

    private String createTransaction() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"categoryId":"%s","type":"EXPENSE","amount":100.00,"description":"Test","date":"2026-05-01"}
                        """.formatted(categoryId))
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(201)
                .extract().jsonPath().getString("id");
    }

    // T1
    @Test
    void create_validTransaction_returns201WithData() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"categoryId":"%s","type":"EXPENSE","amount":50.00,"description":"Lunch","date":"2026-05-01"}
                        """.formatted(categoryId))
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("type", equalTo("EXPENSE"))
                .body("amount", equalTo(50.0f))
                .body("description", equalTo("Lunch"));
    }

    // T2
    @Test
    void create_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"categoryId":"%s","type":"EXPENSE","amount":50.00,"date":"2026-05-01"}
                        """.formatted(categoryId))
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(401);
    }

    // T3
    @Test
    void list_returnsOwnTransactions() {
        createTransaction();
        createTransaction();

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("startDate", "2026-05-01")
                .queryParam("endDate", "2026-05-31")
                .when()
                .get("/api/transactions")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    // T4
    @Test
    void list_withDateFilter_returnsMatchingTransactions() {
        createTransaction();

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("startDate", "2026-05-01")
                .queryParam("endDate", "2026-05-31")
                .when()
                .get("/api/transactions")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    // T5
    @Test
    void update_ownTransaction_returns200WithUpdatedFields() {
        String txnId = createTransaction();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"categoryId":"%s","type":"EXPENSE","amount":200.00,"description":"Updated","date":"2026-05-02"}
                        """.formatted(categoryId))
                .when()
                .put("/api/transactions/" + txnId)
                .then()
                .statusCode(200)
                .body("amount", equalTo(200.0f))
                .body("description", equalTo("Updated"));
    }

    // T6 — cross-tenant: user B cannot update user A's transaction
    @Test
    void update_otherUserTransaction_returns404() {
        String ownerTxnId = createTransaction();

        String otherEmail = "other-" + UUID.randomUUID() + "@test.com";
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Other User","email":"%s","password":"password123"}
                        """.formatted(otherEmail))
                .when()
                .post("/api/auth/register");

        String otherToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"password123"}
                        """.formatted(otherEmail))
                .when()
                .post("/api/auth/login")
                .then()
                .extract().jsonPath().getString("accessToken");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + otherToken)
                .body("""
                        {"categoryId":"%s","type":"EXPENSE","amount":999.00,"date":"2026-05-01"}
                        """.formatted(categoryId))
                .when()
                .put("/api/transactions/" + ownerTxnId)
                .then()
                .statusCode(404);
    }

    // T7
    @Test
    void delete_ownTransaction_returns204() {
        String txnId = createTransaction();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/transactions/" + txnId)
                .then()
                .statusCode(204);
    }

    // T8 — cross-tenant: user B cannot delete user A's transaction
    @Test
    void delete_otherUserTransaction_returns404() {
        String ownerTxnId = createTransaction();

        String otherEmail = "other-" + UUID.randomUUID() + "@test.com";
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Other User","email":"%s","password":"password123"}
                        """.formatted(otherEmail))
                .when()
                .post("/api/auth/register");

        String otherToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"password123"}
                        """.formatted(otherEmail))
                .when()
                .post("/api/auth/login")
                .then()
                .extract().jsonPath().getString("accessToken");

        given()
                .header("Authorization", "Bearer " + otherToken)
                .when()
                .delete("/api/transactions/" + ownerTxnId)
                .then()
                .statusCode(404);
    }

    // T9
    @Test
    void delete_nonexistentTransaction_returns404() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/transactions/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }
}
