package com.orizon.coreapi.config;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logmanager.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Assigns each request an id, exposes it on the MDC (so JSON logs include it
 * as {@code requestId}) and echoes it back as the {@code X-Request-Id}
 * response header. Accepts an inbound {@code X-Request-Id} when one is
 * supplied by the ingress, otherwise generates a UUID.
 *
 * <p>Runs before {@link RateLimitFilter} and {@code JwtAuthenticationFilter}
 * so 401/429 responses still carry the id in their logs.
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 200)
@ApplicationScoped
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    static final String HEADER = "X-Request-Id";
    static final String MDC_KEY = "requestId";
    private static final String CONTEXT_PROPERTY = "requestId";

    // Limit charset and length to avoid log injection from a forged inbound header.
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    @Override
    public void filter(ContainerRequestContext ctx) {
        String inbound = ctx.getHeaderString(HEADER);
        String id = (inbound != null && VALID_ID.matcher(inbound).matches())
                ? inbound
                : UUID.randomUUID().toString();
        ctx.setProperty(CONTEXT_PROPERTY, id);
        MDC.put(MDC_KEY, id);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            Object id = request.getProperty(CONTEXT_PROPERTY);
            if (id != null) {
                response.getHeaders().putSingle(HEADER, id);
            }
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
