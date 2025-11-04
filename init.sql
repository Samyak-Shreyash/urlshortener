-- Create database
CREATE DATABASE IF NOT EXISTS url_shortener;
USE url_shortener;

-- URL table
CREATE TABLE url_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    click_count INT DEFAULT 0,
    INDEX idx_short_code (short_code)
);