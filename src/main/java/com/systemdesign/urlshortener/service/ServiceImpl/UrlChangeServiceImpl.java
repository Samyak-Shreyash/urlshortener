package com.systemdesign.urlshortener.service.ServiceImpl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.systemdesign.urlshortener.model.UrlMapping;
import com.systemdesign.urlshortener.repository.UrlRepository;
import com.systemdesign.urlshortener.service.UrlChangeService;
import com.systemdesign.urlshortener.utils.UrlUtils;

import jakarta.transaction.Transactional;

@Service
public class UrlChangeServiceImpl implements UrlChangeService{
    
    
    private static final Logger logger = LogManager.getLogger(UrlChangeServiceImpl.class);

    
    // private static HashMap<String, UrlMapping> urlMap = new HashMap<>();

    @Autowired
    private UrlRepository urlRepository;

    @Override
    public String getOriginalUrl(String shortCode) {
        logger.info("Checking if short code-{} exist in map",shortCode);
        if(shortCode == null || shortCode.isEmpty() || shortCode.isBlank()) {
            logger.info("short code-{} is null",shortCode);
            return null;
        }
        return this.getMappedUrl(shortCode);
    }

    private String getMappedUrl(String shortCode) {
        logger.info("Retrieving URL Mapping for short code-{}",shortCode);
        if(this.urlRepository.existsByShortCode(shortCode))
        {
            Optional<UrlMapping> optionalMap = this.urlRepository.findByShortCode(shortCode);
            UrlMapping mappedUrl = optionalMap.get();
            return mappedUrl.getShortCode(); 
        }
        return null;
    }

    @Override
    public String getShortCode(String oUrl) {
        if(oUrl==null || oUrl.isEmpty() || oUrl.isBlank())
            return null;
        logger.info("Check if url:{} is mapped",oUrl);
        String urlHash = UrlUtils.hashUrl(oUrl);
        return this.getSavedShortCode(urlHash)==null?this.getSavedShortCode(urlHash): this.createUrlMapping(oUrl, urlHash);
        }
    

    private String getSavedShortCode(String urlHash) {
        Optional<String> shortCode =  this.urlRepository.findShortCodeByLongUrlHash(urlHash);
        if(shortCode==null || shortCode.get()==null || shortCode.get().isEmpty())
            return null;
        logger.info("Retrieved Short Code {} for url-hash: {} from DB", shortCode.get(), urlHash);
        return shortCode.get();
    }


    @Transactional
    private String   createUrlMapping(String url, String urlHash) {
        if(url==null)
            return null;
        String shortCode = UrlUtils.generateShortCode();
        logger.info("Generated new short code {} for Url: {}",shortCode, url);
        UrlMapping new_url_map = new UrlMapping(url, shortCode, urlHash);
        this.urlRepository.save(new_url_map);
        logger.info("New URL Mapping saved {}",new_url_map.toString());
        return new_url_map.getShortCode();
    }
}
