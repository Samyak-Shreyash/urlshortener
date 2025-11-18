package com.systemdesign.urlshortener.health;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppInfoContributor implements InfoContributor {
    
    private final Environment environment;
    
    public AppInfoContributor(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appDetails = new HashMap<>();
        appDetails.put("version", "1.0.0");
        appDetails.put("timestamp", LocalDateTime.now().toString());
        appDetails.put("activeProfiles", environment.getActiveProfiles());
        appDetails.put("instanceId", environment.getProperty("INSTANCE_ID", "unknown"));
        
        builder.withDetail("url-shortener", appDetails);
    }
}