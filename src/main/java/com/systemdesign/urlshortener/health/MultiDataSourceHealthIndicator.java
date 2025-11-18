package com.systemdesign.urlshortener.health;

import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MultiDataSourceHealthIndicator implements HealthIndicator {
    
    private final DataSource writeDataSource;
    private final DataSource readDataSource;
    
    public MultiDataSourceHealthIndicator(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
    }
    
    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        
        // Check write database
        boolean writeDbHealthy = checkDataSource(writeDataSource, "write");
        details.put("writeDatabase", writeDbHealthy ? "UP" : "DOWN");
        
        // Check read database
        boolean readDbHealthy = checkDataSource(readDataSource, "read");
        details.put("readDatabase", readDbHealthy ? "UP" : "DOWN");
        
        if (writeDbHealthy && readDbHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
    
    private boolean checkDataSource(DataSource dataSource, String name) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}