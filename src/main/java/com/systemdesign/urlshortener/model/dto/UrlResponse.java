// UrlResponse.java
package com.systemdesign.urlshortener.model.dto;

public class UrlResponse {
    private String shortCode;
    private String originalUrl;
    
    public UrlResponse() {}
    
    public UrlResponse(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
    }
    
    // getters and setters
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }


    @Override
    public String toString() {
        return "{" +
            " shortCode='" + getShortCode() + "'" +
            ", originalUrl='" + getOriginalUrl() + "'" +
            "}";
    }

}
