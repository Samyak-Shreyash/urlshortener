package com.systemdesign.urlshortener.service;

import org.springframework.stereotype.Service;

@Service
public interface UrlChangeService {

    String getShortCode(String url);

    String getOriginalUrl(String shortUrl);
}
