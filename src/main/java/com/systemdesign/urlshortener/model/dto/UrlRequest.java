package com.systemdesign.urlshortener.model.dto;

public class UrlRequest {
    String url;
    public String getUrl() {
        return url;
    }
    public UrlRequest(String url) {
        this.url = url;
    }

}
