package com.fuelfinder.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitInterceptorTest {

    @Test
    void allowsRequestsUpToTheLimit() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new ObjectMapper());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");

        for (int i = 0; i < 10; i++) {
            assertTrue(interceptor.preHandle(request, response, new Object()));
        }
    }

    @Test
    void blocksRequestsPastTheLimit() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new ObjectMapper());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");

        for (int i = 0; i < 10; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        verify(response).setStatus(429);
        assertTrue(body.toString().contains("Too Many Requests"));
    }

    @Test
    void tracksEachIpSeparately() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new ObjectMapper());

        HttpServletRequest requestFromFirstIp = mock(HttpServletRequest.class);
        when(requestFromFirstIp.getMethod()).thenReturn("POST");
        when(requestFromFirstIp.getRemoteAddr()).thenReturn("203.0.113.5");

        HttpServletRequest requestFromSecondIp = mock(HttpServletRequest.class);
        when(requestFromSecondIp.getMethod()).thenReturn("POST");
        when(requestFromSecondIp.getRemoteAddr()).thenReturn("203.0.113.9");

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        for (int i = 0; i < 10; i++) {
            interceptor.preHandle(requestFromFirstIp, response, new Object());
        }

        // The first IP is now at its limit, but a different IP should be unaffected.
        assertFalse(interceptor.preHandle(requestFromFirstIp, response, new Object()));
        assertTrue(interceptor.preHandle(requestFromSecondIp, response, new Object()));
    }

    @Test
    void neverThrottlesGetRequests() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new ObjectMapper());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");

        for (int i = 0; i < 20; i++) {
            assertTrue(interceptor.preHandle(request, response, new Object()));
        }
    }

    @Test
    void prefersXForwardedForOverRemoteAddr() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new ObjectMapper());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        when(request.getMethod()).thenReturn("POST");
        // Real client IP, per a reverse proxy like Railway's -- first entry in a
        // possible comma-separated chain of proxies.
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.23, 10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1"); // the proxy itself

        for (int i = 0; i < 10; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        assertFalse(interceptor.preHandle(request, response, new Object()));
    }
}
