// UrlResponse.java
package com.systemdesign.urlshortener.model.dto;

public class UrlResponse {
    private String shortUrl;
    private String originalUrl;
    
    public UrlResponse() {}
    
    public UrlResponse(String shortUrl, String originalUrl) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
    }
    
    // getters and setters
    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
    
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
}
