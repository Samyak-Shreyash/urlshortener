package com.systemdesign.urlshortener.service.ServiceImpl;

import java.security.SecureRandom;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.systemdesign.urlshortener.model.UrlMapping;
import com.systemdesign.urlshortener.service.UrlChangeService;

@Service
public class UrlChangeServiceImpl implements UrlChangeService{
    
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();

    @Value("${base.url}")
    private String baseUrl;

    private static HashMap<String, UrlMapping> urlMap = new HashMap<>();

    @Override
    public String getOriginalUrl(String url) {
        
        UrlMapping mappedUrl= urlMap.get(url);
        if(mappedUrl == null) {
            return "URL not found";
        }
        mappedUrl.setClickCount(mappedUrl.getClickCount() + 1);
        urlMap.put(url, mappedUrl);
        return mappedUrl.getOriginalUrl();
    }

    @Override
    public String getShortUrl(String oUrl) {
        String shortCode = this.generateShortUrl();
        UrlMapping mappedUrl = this.createUrlMapping(oUrl, shortCode);
           urlMap.put(shortCode, mappedUrl);            
            return baseUrl.concat(mappedUrl.getShortUrl());
        }
    

    private String generateShortUrl() {
        final int SHORT_URL_LENGTH = 6;
        StringBuilder shortUrl = new StringBuilder(SHORT_URL_LENGTH);

        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            shortUrl.append(ALPHABET.charAt(randomIndex));
        }
        
        return shortUrl.toString();
    }

    private UrlMapping createUrlMapping(String url, String shortUrl) {
        return new UrlMapping(url, shortUrl);
    }
    
}
