-- Master replication setup
SET sql_log_bin = 0;

-- Drop existing users if they exist and recreate with proper permissions
DROP USER IF EXISTS 'root'@'%';
DROP USER IF EXISTS 'repl_user'@'%';
DROP USER IF EXISTS 'app_user'@'%';

-- Create root user that can connect from any host
CREATE USER 'root'@'%' IDENTIFIED BY 'root_password';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- Create replication user that allows connections from any host
CREATE USER 'repl_user'@'%' IDENTIFIED BY 'repl_password';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'repl_user'@'%';

-- Create application user that allows connections from any host
CREATE USER 'app_user'@'%' IDENTIFIED BY 'app_password';
GRANT ALL PRIVILEGES ON url_shortener.* TO 'app_user'@'%';

FLUSH PRIVILEGES;
SET sql_log_bin = 1;

-- Create URL shortener database and table
CREATE DATABASE IF NOT EXISTS url_shortener;
USE url_shortener;
CREATE TABLE IF NOT EXISTS url_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    long_url TEXT NOT NULL,
    long_url_hash CHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_short_code (short_code),
    INDEX idx_long_url_hash (long_url_hash),
) ENGINE=InnoDB;

FLUSH PRIVILEGES;