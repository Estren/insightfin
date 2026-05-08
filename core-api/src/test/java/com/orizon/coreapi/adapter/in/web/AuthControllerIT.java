package com.orizon.coreapi.adapter.in.web;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthControllerIT {

    private static String uniqueEmail() {
        return "u-" + UUID.randomUUID() + "@test.com";
    }

    private static void register(String email) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Test User","email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);
    }

    private static String loginAndGetToken(String email) {
        return given()
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

    // A1
    @Test
    void register_validRequest_returns201WithTokenPair() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Alice","email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    // A2
    @Test
    void register_duplicateEmail_returns409() {
        String email = uniqueEmail();
        register(email);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Another","email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(409);
    }

    // A3
    @Test
    void register_invalidEmail_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Test","email":"not-an-email","password":"password123"}
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400);
    }

    // A4
    @Test
    void register_shortPassword_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Test","email":"%s","password":"short"}
                        """.formatted(uniqueEmail()))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400);
    }

    // A5
    @Test
    void login_validCredentials_returnsTokenPair() {
        String email = uniqueEmail();
        register(email);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    // A6
    @Test
    void login_wrongPassword_returns400() {
        String email = uniqueEmail();
        register(email);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"wrongpassword"}
                        """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }

    // A7
    @Test
    void login_unknownEmail_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"nobody-%s@test.com","password":"password123"}
                        """.formatted(UUID.randomUUID()))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }

    // A8
    @Test
    void refresh_validToken_returnsNewAccessToken() {
        String email = uniqueEmail();
        register(email);

        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"password123"}
                        """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .extract().jsonPath().getString("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"refreshToken":"%s"}
                        """.formatted(refreshToken))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue());
    }

    // A9
    @Test
    void refresh_invalidToken_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"refreshToken":"invalid-token-that-does-not-exist-in-db"}
                        """)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(400);
    }

    // A10
    @Test
    void logout_withValidToken_returns204() {
        String email = uniqueEmail();
        register(email);
        String token = loginAndGetToken(email);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(204);
    }

    // A11
    @Test
    void logout_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(401);
    }
}
