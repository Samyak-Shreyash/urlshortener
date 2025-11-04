package com.systemdesign.urlshortener.model.dto;

// ErrorResponse.java
public class ErrorResponse {
    private String error;
    private int code;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String error, int code) {
        this.error = error;
        this.code = code;
    }
    
    // getters and setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
}