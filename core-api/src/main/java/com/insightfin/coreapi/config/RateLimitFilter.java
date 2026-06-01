package com.insightfin.coreapi.config;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiting on the unauthenticated auth endpoints, to blunt brute
 * force and e-mail abuse. In-memory fixed-window counter — state is per replica
 * (acceptable trade-off: it still raises the bar significantly without Redis).
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
@ApplicationScoped
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "api/auth/login",
            "api/auth/register",
            "api/auth/forgot-password",
            "api/auth/resend-verification",
            // F-05: also gate /reset-password to keep token brute-force impractical
            // (UUID v4 token + 30-min TTL is already hard, but inclusion is cheap)
            "api/auth/reset-password");

    private static final long WINDOW_MILLIS = 60_000L;
    private static final int MAX_TRACKED_IPS = 10_000;

    @ConfigProperty(name = "app.rate-limit.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "app.rate-limit.requests-per-minute", defaultValue = "10")
    int requestsPerMinute;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext ctx) {
        if (!enabled || !isLimited(ctx.getUriInfo().getPath())) {
            return;
        }

        if (!allow(clientIp(ctx))) {
            ctx.abortWith(Response.status(429)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 429,
                            "error", "Too Many Requests",
                            "message", "Rate limit exceeded. Please try again in a minute."))
                    .build());
        }
    }

    private boolean isLimited(String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        for (String limited : LIMITED_PATHS) {
            if (normalized.startsWith(limited)) {
                return true;
            }
        }
        return false;
    }

    private boolean allow(String ip) {
        long now = System.currentTimeMillis();
        if (windows.size() > MAX_TRACKED_IPS) {
            windows.clear();
        }
        Window window = windows.computeIfAbsent(ip, k -> new Window(now));
        synchronized (window) {
            if (now - window.windowStart >= WINDOW_MILLIS) {
                window.windowStart = now;
                window.count = 0;
            }
            window.count++;
            return window.count <= requestsPerMinute;
        }
    }

    private static String clientIp(ContainerRequestContext ctx) {
        String forwarded = ctx.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = ctx.getHeaderString("X-Real-IP");
        return (realIp != null && !realIp.isBlank()) ? realIp.trim() : "unknown";
    }

    private static final class Window {
        long windowStart;
        int count;

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
