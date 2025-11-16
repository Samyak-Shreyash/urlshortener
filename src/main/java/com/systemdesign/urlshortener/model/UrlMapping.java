package com.systemdesign.urlshortener.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "url_mappings", 
       indexes = {
           @Index(name = "idx_short_code", columnList = "shortCode"),
           @Index(name = "idx_long_url_hash", columnList = "longUrlHash")
       })
public class UrlMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;
    
    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;
    
    @Column(name = "long_url_hash", nullable = false, length = 64)
    private String longUrlHash;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public UrlMapping() {}
    
    public UrlMapping(String shortCode, String longUrl, String longUrlHash) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.longUrlHash = longUrlHash;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public String getLongUrl() {
        return longUrl;
    }
    
    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
    
    public String getLongUrlHash() {
        return longUrlHash;
    }
    
    public void setLongUrlHash(String longUrlHash) {
        this.longUrlHash = longUrlHash;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "UrlMapping{" +
                "id=" + id +
                ", shortCode='" + shortCode + '\'' +
                ", longUrl='" + longUrl + '\'' +
                ", longUrlHash='" + longUrlHash + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}