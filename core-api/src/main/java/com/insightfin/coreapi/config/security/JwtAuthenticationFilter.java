package com.insightfin.coreapi.config.security;

import com.insightfin.coreapi.domain.model.Role;
import com.insightfin.coreapi.domain.port.out.TokenProvider;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    @Inject
    TokenProvider tokenProvider;

    @Inject
    AuthenticatedUser authenticatedUser;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        if (isPublicPath(path)) {
            return;
        }

        String header = requestContext.getHeaderString("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (tokenProvider.isValid(token)) {
                UUID userId = tokenProvider.extractUserId(token);
                Role role = tokenProvider.extractRole(token);
                boolean emailVerified = tokenProvider.isEmailVerified(token);
                authenticatedUser.setUserId(userId);
                authenticatedUser.setRole(role);

                if (!emailVerified && !isVerificationAllowedPath(path)) {
                    requestContext.abortWith(
                            Response.status(Response.Status.FORBIDDEN)
                                    .type(MediaType.APPLICATION_JSON)
                                    .entity(Map.of(
                                            "timestamp", LocalDateTime.now().toString(),
                                            "status", 403,
                                            "error", "Forbidden",
                                            "error_code", "EMAIL_NOT_VERIFIED",
                                            "message", "Verify your email to access this resource."
                                    ))
                                    .build());
                    return;
                }

                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> userId.toString();
                    }

                    @Override
                    public boolean isUserInRole(String roleName) {
                        return role != null && role.name().equals(roleName);
                    }

                    @Override
                    public boolean isSecure() {
                        return requestContext.getSecurityContext().isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "Bearer";
                    }
                });
                return;
            }
        }

        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private boolean isPublicPath(String path) {
        String normalized = normalize(path);
        return normalized.startsWith("api/auth/login")
                || normalized.startsWith("api/auth/register")
                || normalized.startsWith("api/auth/google")
                || normalized.startsWith("api/auth/refresh")
                || normalized.startsWith("api/auth/forgot-password")
                || normalized.startsWith("api/auth/reset-password")
                || normalized.startsWith("api/auth/verify-email")
                || normalized.startsWith("api/auth/verify-email-pin")
                || normalized.startsWith("api/auth/resend-verification")
                || normalized.startsWith("internal")
                || normalized.startsWith("swagger-ui")
                || normalized.startsWith("q/")
                || normalized.startsWith("v3/api-docs")
                || normalized.startsWith("openapi");
    }

    private boolean isVerificationAllowedPath(String path) {
        String normalized = normalize(path);
        if (isPublicPath(path)) return true;
        // Allow logout so unverified users can always leave the app.
        if (normalized.startsWith("api/auth/logout")) return true;
        // Allow reading own profile so the frontend can render the user's name/email.
        return normalized.startsWith("api/users/me") && !normalized.contains("/email/");
    }

    private String normalize(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
