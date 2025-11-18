package com.systemdesign.urlshortener.health;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up()
                    .withDetail("database", "Connected successfully")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Connection is not valid")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Connection failed: " + e.getMessage())
                .withException(e)
                .build();
        }
    }
}