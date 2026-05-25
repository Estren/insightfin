package com.insightfin.coreapi.adapter.in.web;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CategoryControllerIT {

    private String token;

    @BeforeEach
    void setUp() {
        String email = "cat-" + UUID.randomUUID() + "@test.com";

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
    }

    private String createCategory(String name, String type) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"name":"%s","type":"%s","icon":"icon","color":"#000000"}
                        """.formatted(name, type))
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .extract().jsonPath().getString("id");
    }

    // C1
    @Test
    void create_validCategory_returns201WithData() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"name":"Groceries","type":"EXPENSE","icon":"cart","color":"#00FF00"}
                        """)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Groceries"))
                .body("type", equalTo("EXPENSE"));
    }

    // C2
    @Test
    void create_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Groceries","type":"EXPENSE"}
                        """)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(401);
    }

    // C3
    @Test
    void list_returnsOwnCategories() {
        createCategory("Food", "EXPENSE");
        createCategory("Salary", "INCOME");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    // C4
    @Test
    void list_filteredByType_returnsOnlyMatchingType() {
        createCategory("Rent", "EXPENSE");
        createCategory("Freelance", "INCOME");

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("type", "EXPENSE")
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("findAll { it.type == 'EXPENSE' }.size()", greaterThanOrEqualTo(1))
                .body("findAll { it.type == 'INCOME' }.size()", equalTo(0));
    }

    // C5
    @Test
    void update_ownCategory_returns200WithUpdatedFields() {
        String catId = createCategory("OldName", "EXPENSE");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"name":"NewName","type":"INCOME","icon":"new-icon","color":"#FF00FF"}
                        """)
                .when()
                .put("/api/categories/" + catId)
                .then()
                .statusCode(200)
                .body("name", equalTo("NewName"))
                .body("type", equalTo("INCOME"));
    }

    // C6 — cross-tenant: user B cannot update user A's category
    @Test
    void update_otherUserCategory_returns404() {
        String ownerCatId = createCategory("MyCategory", "EXPENSE");

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
                        {"name":"Hacked","type":"EXPENSE","icon":"x","color":"#000000"}
                        """)
                .when()
                .put("/api/categories/" + ownerCatId)
                .then()
                .statusCode(404);
    }

    // C7
    @Test
    void delete_ownCategory_returns204() {
        String catId = createCategory("ToDelete", "EXPENSE");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/categories/" + catId)
                .then()
                .statusCode(204);
    }

    // C8 — cross-tenant: user B cannot delete user A's category
    @Test
    void delete_otherUserCategory_returns404() {
        String ownerCatId = createCategory("MyCategory", "INCOME");

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
                .delete("/api/categories/" + ownerCatId)
                .then()
                .statusCode(404);
    }
}
