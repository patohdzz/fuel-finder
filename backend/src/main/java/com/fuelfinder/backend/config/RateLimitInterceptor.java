package com.fuelfinder.backend.config;

import com.fuelfinder.backend.dto.ApiErrorResponse;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

// Simple per-IP fixed-window rate limiter -- protects the public,
// unauthenticated price-report endpoint from being spammed. Deliberately
// in-memory rather than a library/external store: traffic here is low,
// and a single Railway instance doesn't need distributed rate limiting.
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, RequestWindow> requestWindows = new ConcurrentHashMap<>();

    public RateLimitInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // Only writes get throttled -- reads (GET) are left alone.
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = resolveClientIp(request);
        RequestWindow window = requestWindows.computeIfAbsent(clientIp, ip -> new RequestWindow());

        if (!window.tryConsume()) {
            respondTooManyRequests(response);
            return false;
        }

        return true;
    }

    // Railway (and most hosts) put requests through a reverse proxy, so
    // request.getRemoteAddr() would just return the proxy's own address --
    // the real client IP shows up in X-Forwarded-For instead.
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private void respondTooManyRequests(HttpServletResponse response) throws IOException {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                429,
                "Too Many Requests",
                "Too many price updates from this address -- please wait a minute and try again.",
                LocalDateTime.now());

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    // Tracks how many requests one IP has made in the current window,
    // resetting once the window elapses. Traffic here is low enough that
    // a synchronized method is simpler than a lock-free approach and not
    // a real performance concern.
    private static class RequestWindow {
        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();

            if (now - windowStart > WINDOW_MILLIS) {
                windowStart = now;
                count = 0;
            }

            if (count >= MAX_REQUESTS_PER_WINDOW) {
                return false;
            }

            count++;
            return true;
        }
    }
}
