package com.systemdesign.urlshortener.controller;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.systemdesign.urlshortener.model.dto.ErrorResponse;
import com.systemdesign.urlshortener.model.dto.UrlRequest;
import com.systemdesign.urlshortener.model.dto.UrlResponse;
import com.systemdesign.urlshortener.service.UrlChangeService;

@RestController
public class UrlChanger {

    private static final Logger logger = LoggerFactory.getLogger(UrlChanger.class);
    
    @Autowired
    private UrlChangeService urlChangeService;

    @GetMapping("/api/{shortCode}")
    public ResponseEntity<?> getUrl(@PathVariable String shortCode){
        String correlationId = MDC.get("correlationId");
        Instant now = Instant.now();
        logger.info("[{}] - Request received for URL retrieval with short code: {}", correlationId, shortCode);
            try  {
            String originalUrl = this.urlChangeService.getOriginalUrl(shortCode);
            if (originalUrl != null) {
                UrlResponse response = new UrlResponse(shortCode, originalUrl);
                logger.info("[{}] - Returning Original URL: {} for Short Code: {} in {} ms", correlationId, originalUrl, shortCode, Duration.between(now, Instant.now()).toMillis());
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("Short URL not found", 404);
                logger.error("[{}] - Error: {}", correlationId, error.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Server error: " + e.getMessage(), 500);
            logger.error("[{}] - Error: {}", correlationId, error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> getShortCode(@NonNull @RequestBody UrlRequest url){
        String correlationId = MDC.get("correlationId");
        long startTime = System.nanoTime();
        try {
            if (url.getUrl() == null) {
                ErrorResponse error = new ErrorResponse("URL is required", 400);
                logger.error("[{}] - Error: {}",correlationId,  error.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
            
            String inputUrl = url.getUrl();
            String shortCode = this.urlChangeService.getShortCode(inputUrl);
            long executionTime = (System.nanoTime() - startTime) / 1000; // Convert nanoseconds to milliseconds
            logger.info("[{}] - Generated Short Code: {} for Original URL: {} in {} ms", correlationId, shortCode, inputUrl, executionTime);
            
            UrlResponse response = new UrlResponse(shortCode, inputUrl);
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            logger.error("[{}] - Url: {} Error: {}", correlationId, url.getUrl(), error.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Server error: " + e.getMessage(), 500);
            logger.error("[{}] - Url: {} Error: {}", correlationId, url.getUrl(), error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
