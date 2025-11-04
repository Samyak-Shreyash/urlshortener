package com.systemdesign.urlshortener.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private UrlChangeService urlChangeService;

    @GetMapping("/api/{shortUrl}")
    public ResponseEntity<?> getUrl(@PathVariable String shortUrl){
            try {
            String originalUrl = this.urlChangeService.getOriginalUrl(shortUrl);
            if (originalUrl != null) {
                UrlResponse response = new UrlResponse();
                response.setOriginalUrl(originalUrl);
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("Short URL not found", 404);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Server error: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

     @PostMapping("/api/shorten")
    public ResponseEntity<?> getShortUrl(@RequestBody UrlRequest url) {
        try {
            // Validate input
            if (url == null || url.getUrl() == null || url.getUrl().trim().isEmpty()) {
                ErrorResponse error = new ErrorResponse("URL is required", 400);
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validate URL format
            String inputUrl = url.getUrl();
            if (!isValidUrl(inputUrl)) {
                ErrorResponse error = new ErrorResponse("Invalid URL format", 400);
                return ResponseEntity.badRequest().body(error);
            }
            
            String shortUrl = this.urlChangeService.getShortUrl(inputUrl);
            UrlResponse response = new UrlResponse(shortUrl, inputUrl);
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Server error: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
