package com.staysphere.payment.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Every endpoint on this service is meant to be called ONLY by the main
// StaySphere backend, server-to-server — there is no per-user auth here.
// Without this check, anyone who can reach this port could create Razorpay
// orders or read any booking's payment history.
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api-key}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Razorpay calls the webhook directly — it can't send our internal key,
        // so that route is authenticated by its own HMAC signature instead.
        if (request.getRequestURI().equals("/api/payments/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader("X-Internal-Api-Key");
        if (expectedApiKey == null || !expectedApiKey.equals(providedKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Missing or invalid internal API key\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}