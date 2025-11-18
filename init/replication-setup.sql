-- Master replication setup
SET sql_log_bin = 0;

-- Create replication user with proper privileges
CREATE USER IF NOT EXISTS '${MYSQL_REPLICATION_USER}'@'%' IDENTIFIED BY '${MYSQL_REPLICATION_PASSWORD}';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '${MYSQL_REPLICATION_USER}'@'%';

-- Create application user
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_USER_PASSWORD}';
GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';

-- Enable binary logging and GTID
SET GLOBAL gtid_mode = ON;
SET GLOBAL enforce_gtid_consistency = ON;

FLUSH PRIVILEGES;
SET sql_log_bin = 1;

-- Create URL shortener table
CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};
USE ${MYSQL_DATABASE};

CREATE TABLE IF NOT EXISTS url_mappings (
    short_code VARCHAR(10) PRIMARY KEY,
    long_url TEXT NOT NULL,
    long_url_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_long_url_hash (long_url_hash),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;