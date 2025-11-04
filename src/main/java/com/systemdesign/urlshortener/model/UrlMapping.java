package com.systemdesign.urlshortener.model;

public class UrlMapping {

    private String originalUrl;

    private String shortUrl;

    private int clickCount;

    public UrlMapping() {}

    public UrlMapping(String originalUrl, String shortUrl) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.clickCount=0;
    }

    public String getOriginalUrl() {
        return this.originalUrl;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }
    
    public String getShortUrl() {
        return this.shortUrl;
    }

}
