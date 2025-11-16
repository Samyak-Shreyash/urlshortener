package com.systemdesign.urlshortener.model.dto;

// ErrorResponse.java
public class ErrorResponse {
    private String message;
    private int code;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }
    
    // getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
}