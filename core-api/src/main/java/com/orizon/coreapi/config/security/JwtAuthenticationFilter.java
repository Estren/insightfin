package com.orizon.coreapi.config.security;

import com.orizon.coreapi.domain.port.out.TokenProvider;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;
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
                authenticatedUser.setUserId(userId);

                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> userId.toString();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return true;
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
        return path.startsWith("api/auth/login")
                || path.startsWith("api/auth/register")
                || path.startsWith("api/auth/refresh")
                || path.startsWith("swagger-ui")
                || path.startsWith("q/")
                || path.startsWith("v3/api-docs")
                || path.startsWith("openapi");
    }
}
