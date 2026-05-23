package com.orizon.coreapi.config;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class RequestIdFilterIT {

    private static final String UUID_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    // Hit a JAX-RS route (not /q/* extension endpoints, which bypass JAX-RS filters).
    // The 401 from JwtAuthenticationFilter is fine — response filter still runs on aborts.
    private static final String PROBE = "/api/users/me";

    @Test
    void missingHeader_generatesUuidAndEchoesIt() {
        given()
                .when().get(PROBE)
                .then()
                .header("X-Request-Id", matchesPattern(UUID_PATTERN));
    }

    @Test
    void validInboundHeader_isPreserved() {
        String inbound = "ingress-abc-123";

        given()
                .header("X-Request-Id", inbound)
                .when().get(PROBE)
                .then()
                .header("X-Request-Id", equalTo(inbound));
    }

    @Test
    void invalidInboundHeader_isReplacedWithGeneratedId() {
        // Contains characters outside the allowlist — must be rejected.
        given()
                .header("X-Request-Id", "bad id with spaces and <tag>")
                .when().get(PROBE)
                .then()
                .header("X-Request-Id", matchesPattern(UUID_PATTERN));
    }
}
