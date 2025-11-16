package com.systemdesign.urlshortener.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter implements Filter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String correlationId = getCorrelationIdFromHeader(httpRequest);
        
        try (MDC.MDCCloseable closeable = MDC.putCloseable(CORRELATION_ID_MDC_KEY, correlationId)) {
            // Add correlation ID to response headers for client tracking
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            long startTime = System.currentTimeMillis();
            
            // Log request start
            logRequestStart(httpRequest, correlationId);
            
            chain.doFilter(request, response);
            
            // Log request completion
            long duration = System.currentTimeMillis() - startTime;
            logRequestCompletion(httpRequest, correlationId, duration, httpResponse.getStatus());
            
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
    
    private String getCorrelationIdFromHeader(HttpServletRequest httpRequest) {
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        return correlationId != null ? correlationId : generateCorrelationId();
    }
    
    private String generateCorrelationId() {
        return "app_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void logRequestStart(HttpServletRequest request, String correlationId) {
        System.out.printf("REQUEST_START [%s] %s %s %s%n",
            correlationId,
            request.getMethod(),
            request.getRequestURI(),
            getClientInfo(request));
    }
    
    private void logRequestCompletion(HttpServletRequest request, String correlationId, 
                                    long duration, int statusCode) {
        System.out.printf("REQUEST_END [%s] %s %s - Status: %d - Duration: %dms%n",
            correlationId,
            request.getMethod(),
            request.getRequestURI(),
            statusCode,
            duration);
    }
    
    private String getClientInfo(HttpServletRequest request) {
        return String.format("(IP: %s)", request.getRemoteAddr());
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }
    
    @Override
    public void destroy() {
        // Cleanup logic if needed
    }

}