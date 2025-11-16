package com.systemdesign.urlshortener.controller;

import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(UrlChanger.class);
    private static final String UNIT = "µs";


    @GetMapping("/api/{shortCode}")
    public ResponseEntity<?> getUrl(@PathVariable String shortCode){
        Instant now = Instant.now();
        logger.info("Getting Original URl back for Short Code: {}",shortCode);
            try {
            String originalUrl = this.urlChangeService.getOriginalUrl(shortCode);
            if (originalUrl != null) {
                UrlResponse response = new UrlResponse(shortCode, originalUrl);
                logger.info("Original URl: {} for Short Code: {} in {} {}",originalUrl, shortCode, 
                (Duration.between(now, Instant.now()).toMillis()*1000), UNIT);
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("Short URL not found", 404);
                logger.error(error);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Server error: " + e.getMessage(), 500);
            logger.error(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

     @PostMapping("/api/shorten")
    public ResponseEntity<?> getShortCode(@RequestBody UrlRequest url) {
        Long stTime = System.nanoTime();
        try {
            // Validate input
            if (url ==null || url.getUrl() == null) {
                ErrorResponse error = new ErrorResponse("URL is required", 400);
                logger.error(error);
                return ResponseEntity.badRequest().body(error);
            }         
            // Validate URL format
            String inputUrl = url.getUrl();
            String shortCode = this.urlChangeService.getShortCode(inputUrl);
            logger.info("Short Code: {} for Original URl: {} in {} {}",shortCode, inputUrl, 
            (System.nanoTime()-stTime)/1000, UNIT);
            UrlResponse response = new UrlResponse(shortCode, inputUrl);
            // logger.info("Url Response: {}", response.toString()); 
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            logger.error(error);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Server error: " + e.getMessage(), 500);
            logger.error(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
